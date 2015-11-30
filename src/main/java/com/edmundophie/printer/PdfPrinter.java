package com.edmundophie.printer;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.tools.generic.MathTool;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import javax.xml.bind.Element;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by edmundophie on 11/27/15.
 */
public class PdfPrinter {
    private final static String VELOCITY_TICKET_TEMPLATE = "ticket_template.vm";
    private final static String GENERATED_TICKET_FILENAME = "generated_ticket.html";
    private static VelocityEngine velocityEngine;
    private static VelocityContext velocityContext;
    private static Document htmlSourceDoc;
    private static BufferedReader inputReader;
    private static List passengers;
    private static List itineraries;
    private static Elements requiredElements;
    private static String input;

    public static void main(String[] args) throws IOException {
        initVelocity();
        initParser();
        init();

        getPassengerDetails();
        getItineraryDetails();
        getOtherDetails();

        generateHtml();
    }

    private static void initVelocity() {
        velocityEngine = new VelocityEngine();
        velocityEngine .setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        velocityEngine .setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
        velocityEngine .init();
        velocityContext = new VelocityContext();
        velocityContext.put("math", new MathTool());
    }

    private static void initParser() throws IOException {
        htmlSourceDoc = Jsoup.parse(new File(PdfPrinter.class.getClassLoader().getResource(VELOCITY_TICKET_TEMPLATE).getFile()), "UTF-8");
        requiredElements = htmlSourceDoc.select(".required");
    }

    private static void init() {
        inputReader = new BufferedReader(new InputStreamReader(System.in));
        passengers = new ArrayList();
        itineraries = new ArrayList();
    }

    private static void generateHtml() throws FileNotFoundException, UnsupportedEncodingException {
        Template template = velocityEngine.getTemplate(VELOCITY_TICKET_TEMPLATE);
        StringWriter writer = new StringWriter();
        template.merge(velocityContext, writer);

        PrintWriter fileWriter = new PrintWriter(GENERATED_TICKET_FILENAME, "UTF-8");
        fileWriter.println(writer.toString());
        fileWriter.close();
    }

    private static void getPassengerDetails() throws IOException {
        Elements passengerRequiredElements = htmlSourceDoc.select(".passenger-details table tr > .looping-required");
        System.out.print("Passenger count: ");
        input = inputReader.readLine().trim();
        int passengerCount = Integer.parseInt(input);

        for(int i=0;i<passengerCount;++i) {
            Map passenger= new HashMap();
            for(org.jsoup.nodes.Element itDetailElmt:passengerRequiredElements) {
                System.out.print(itDetailElmt.id() + ": ");
                input = inputReader.readLine().trim();
                passenger.put(itDetailElmt.id(), input);
            }
            passengers.add(passenger);
        }
        velocityContext.put("passengers", passengers);
    }

    private static void getItineraryDetails() throws IOException {
        Elements itineraryRequiredElements = htmlSourceDoc.select(".itinerary-details table tr > .looping-required");
        System.out.print("Itinerary count: ");
        input = inputReader.readLine().trim();
        int itineraryCount = Integer.parseInt(input);

        for(int i=0;i<itineraryCount;++i) {
            Map itinerary = new HashMap();
            for(org.jsoup.nodes.Element itDetailElmt:itineraryRequiredElements) {
                System.out.print(itDetailElmt.id() + ": ");
                input = inputReader.readLine().trim();
                itinerary.put(itDetailElmt.id(), input);
            }
            itineraries.add(itinerary);
        }
        velocityContext.put("itineraries", itineraries);
    }

    private static void getOtherDetails() throws IOException {
        Elements detailsRequiredElements = htmlSourceDoc.select(".required");

        for(org.jsoup.nodes.Element itDetailElmt:detailsRequiredElements) {
            System.out.print(itDetailElmt.id() + ": ");
            input = inputReader.readLine().trim();
            velocityContext.put(itDetailElmt.id(), input);
        }
    }
}
