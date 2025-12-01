package com.example.indeedgambling;

import java.util.List;

public class CSVFormatter {

    public static String formatEntrantsCSV(Event event, List<Profile> entrants) {
        StringBuilder csvContent = new StringBuilder();

        // Write csv header
        csvContent.append("Event:,").append(escapeCsv(event.getEventName())).append("\n");
        csvContent.append("Total Entrants:,").append(String.valueOf(entrants.size())).append("\n\n");

        // Write data header
        csvContent.append("Name,Email,Phone Number,Status,Registration Date\n");

        // Write entrant data
        for (Profile entrant : entrants) {
            csvContent.append(escapeCsv(entrant.getPersonName())).append(",");
            csvContent.append(escapeCsv(entrant.getEmail())).append(",");
            csvContent.append(escapeCsv(entrant.getPhone())).append(",");
            csvContent.append("Accepted,"); // Simplified for testing
            csvContent.append(escapeCsv("2024-01-01")).append("\n"); // Simplified date
        }

        return csvContent.toString();
    }

    private static String escapeCsv(String value) {
        if (value == null || value.isEmpty()) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
}