package zendesk.ticketviewer;

import com.google.gson.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Scanner;

public class TicketViewer {
    Scanner scanner;
    Ticket[] tickets;
    public TicketViewer(Scanner scanner){
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        System.out.println("Welcome to the ZenDesk Ticket Viewer");
        Scanner scanner = null;
        if(args != null){
            File file = new File(args[0]);
            try {
                scanner = new Scanner(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }else{
            scanner = new Scanner(System.in);
        }
        TicketViewer viewer = new TicketViewer(scanner);
        boolean shutdown = false;
        while(!shutdown){
            System.out.println("Type 'menu' for options or 'quit' to close the viewer");
            String input = scanner.next();
            switch(input){
                case "menu":
                    printOptions();

                    break;
                case "quit":
                    System.out.println("Thanks for using the ticket viewer!");
                    shutdown = true;
                    break;
                case "1":
                    viewer.printAllTickets();
                    break;
                case "2":
                    viewer.printSpecificTicket();
                default:
                    System.out.println("That input is invalid. Please try again!");
                    printOptions();
                    break;
            }
        }
    }

    private static void printOptions(){
        System.out.println("    Viewing Options:");
        System.out.println("    * Press '1' to view a list of all tickets");
        System.out.println("    * Press '2' to view a specific ticket with details");
        System.out.println("    * Type 'quit' to exit the viewer");
    }

    private void printAllTickets(){
        try{
            tickets = getAllTickets();
        }catch(Exception e){
            System.out.println("Fetching tickets failed");
            System.out.println("Now showing the last set of fetched tickets");
        }
        if(tickets == null){
            System.out.println("No tickets have need fetched");
            return;
        }
        int page = 0;
        printPage(page);
        if(this.tickets.length <= 25) return;
        boolean printing = true;
        while (printing){
            System.out.println("Type 'next' to continue to the next page");
            System.out.println("Type 'prev' to return to the previous page");
            System.out.println("Type 'reload' to fetch updated ticket list");
            System.out.println("Type 'exit' to return to main menu");
            switch (scanner.next()){
                case "next":
                    page++;
                    if(Math.ceil(((double)tickets.length)/25.00) < page){
                        System.out.println("Page out of bounds");
                        page--;
                        break;
                    }
                    printPage(page);
                    break;
                case "prev":
                    page--;
                    if(page < 0){
                        System.out.println("Page out of bounds");
                        page++;
                        break;
                    }
                    printPage(page);
                    break;
                case "reload":
                    try{
                        tickets = getAllTickets();
                    }catch(Exception e){
                        System.out.println("Fetching tickets failed");
                        System.out.println("Now showing the last set of fetched tickets");
                    }
                    page = 0;
                    break;
                case "exit":
                    System.out.println("Exiting print menu.");
                    printing = false;
                    break;
                default:
                    System.out.println("That input is invalid. Please try again!");
                    break;
            }
        }
    }

    private void printPage(int page){
        if(tickets == null) return;
        System.out.println("Page: " + (page+1));
        for(int i = 25*page;i < this.tickets.length && i < 25*(page+1); i++){
            Ticket ticket = tickets[i];
            System.out.println("Ticket " + ticket.requesterId + ": with subject '" + ticket.subject + "'");
        }
    }

    private Ticket[] getAllTickets() throws Exception {
        String plainCredentials = "binyamin.jachter@gmail.com:differentPassword";
        String base64Credentials = new String(Base64.getEncoder().encode(plainCredentials.getBytes()));
        // Create authorization header
        String authorizationHeader = "Basic " + base64Credentials;
        //set up TCP connection
        HttpRequest.Builder builder;
            builder = HttpRequest.newBuilder()
                    .uri(new URI("https://zccjachter.zendesk.com/api/v2/tickets"))
                    .timeout(Duration.ofSeconds(10))
                    .header("Authorization", authorizationHeader)
                    .header("Accept", "application/json")
                    .GET();
        HttpRequest request = builder.build();
        HttpResponse<String> response = HttpClient.newBuilder().build().send(request, HttpResponse.BodyHandlers.ofString());
        // get JSON
        String body = response.body();
        //parse JSON into String
        Gson gson = new Gson();
        JsonObject json = new JsonParser().parse(body).getAsJsonObject();
        JsonArray array = json.getAsJsonArray("tickets");
        ArrayList<Ticket> temp = new ArrayList<>(array.size());
        for (JsonElement element : array) {
            JsonObject obj = element.getAsJsonObject();
            int requesterID = obj.get("requester_id").getAsInt();
            int assigneeID = obj.get("assignee_id").getAsInt();
            String subject = obj.get("subject").getAsString();
            String description = obj.get("description").getAsString();
            Ticket ticket = new Ticket(requesterID, assigneeID, subject, description);
            temp.add(ticket);
        }
        return temp.toArray(new Ticket[array.size()]);
    }

    private void printSpecificTicket(){
        String num = scanner.next();
        if(Character.isDigit(num.charAt(0))){
            int index= Integer.parseInt(num);
            Ticket ticket = null;
            try{
                ticket = tickets[index];
            }
            catch(IndexOutOfBoundsException e){
                System.out.println("No such ticket at index " + num);
            }
            System.out.println("Ticket " + num + ":");
            System.out.println("    Requester ID: " + ticket.getRequesterId());
            System.out.println("    Assignee ID: " + ticket.getAssigneeId());
            System.out.println("    Subject: " + ticket.getSubject());
            System.out.println("    Description: " + ticket.getDescription());
        }else{
            System.out.println("That isn't a valid input.");
        }
    }

    private static class Ticket{
        private final int requesterId;
        private final int assigneeId;
        private final String subject;
        private final String description;

        Ticket(int requesterId, int assigneeId, String subject, String description){
            this.requesterId = requesterId;
            this.assigneeId = assigneeId;
            this.subject = subject;
            this.description = description;
        }
        public int getRequesterId() {
            return requesterId;
        }

        public int getAssigneeId() {
            return assigneeId;
        }

        public String getSubject() {
            return subject;
        }

        public String getDescription() {
            return description;
        }
    }
}

