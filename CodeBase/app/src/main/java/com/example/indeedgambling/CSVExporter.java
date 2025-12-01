package com.example.indeedgambling;




import android.content.Context;
import android.os.Environment;
import android.util.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


/**
 * Handles exporting event entrants to CSV files
 * Creates CSV files in the Downloads folder with event and entrant info
 */


public class CSVExporter {
    /**
     * Exports a list of event entrants to a CSV file
     *
     * @param context  Android context for file operations
     * @param event    The event to export entrants from
     * @param entrants List of people who enrolled in the event
     * @return true if successful, false if failed
     */




    public static boolean exportAllEnrolledEntrants(Context context, Event event, List<Profile> entrants) {
        try {
            // create filename with event name + time
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "All_Enrolled_Entrants_" + sanitizeFileName(event.getEventName()) + "_" + timeStamp + ".csv";


            // get directory
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);


            FileWriter writer = new FileWriter(file);


            // writing csv header
            // calculate totals for each list
            int totalAccepted = event.getAcceptedEntrants() != null ? event.getAcceptedEntrants().size() : 0;
            int totalWaitlisted = event.getWaitingList() != null ? event.getWaitingList().size() : 0;
            int totalInvited = event.getInvitedList() != null ? event.getInvitedList().size() : 0;


            // write csv header with quantity breakdowns
            writer.append("Event:,").append(escapeCsv(event.getEventName())).append("\n");
            writer.append("Export Date:,").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n");
            writer.append("Total Enrolled Entrants:,").append(String.valueOf(entrants.size())).append("\n");
            writer.append("Total Accepted Entrants:,").append(String.valueOf(totalAccepted)).append("\n");
            writer.append("Total Waitlisted Entrants:,").append(String.valueOf(totalWaitlisted)).append("\n");
            writer.append("Total Invited Entrants:,").append(String.valueOf(totalInvited)).append("\n\n");
            // write data header
            writer.append("Name,Email,Phone Number,Status,Registration Date\n");


            // write entrant data
            for (Profile entrant : entrants) {
                writer.append(escapeCsv(entrant.getPersonName())).append(",");
                writer.append(escapeCsv(entrant.getEmail())).append(",");
                writer.append(escapeCsv(entrant.getPhone())).append(",");
                writer.append(escapeCsv(getEntrantStatus(event, entrant))).append(",");
                writer.append(escapeCsv(getRegistrationDate(event, entrant))).append("\n");
            }


            writer.flush();
            writer.close();


            Log.d("CSV_EXPORT", "All enrolled entrants CSV exported to: " + file.getAbsolutePath());
            return true;


        } catch (IOException e) {
            Log.e("CSV_EXPORT", "Error exporting CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    /**
     * Exports ONLY accepted entrants to CSV
     * For organizers who want just the final attending list
     *
     * @param context  Android context for file operations
     * @param event    The event to export entrants from
     * @param entrants List of accepted entrants only
     * @return true if successful, false if failed
     */
    public static boolean exportAcceptedEntrants(Context context, Event event, List<Profile> entrants) {
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "Accepted_Entrants_" + sanitizeFileName(event.getEventName()) + "_" + timeStamp + ".csv";


            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            File file = new File(downloadsDir, fileName);


            FileWriter writer = new FileWriter(file);


            // write csv header
            writer.append("Event:,").append(escapeCsv(event.getEventName())).append("\n");
            writer.append("Export Date:,").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date())).append("\n");
            writer.append("Total Accepted Entrants:,").append(String.valueOf(entrants.size())).append("\n\n");


            // write data header
            writer.append("Name,Email,Phone Number,Registration Date\n");


            // write entrant data
            for (Profile entrant : entrants) {
                writer.append(escapeCsv(entrant.getPersonName())).append(",");
                writer.append(escapeCsv(entrant.getEmail())).append(",");
                writer.append(escapeCsv(entrant.getPhone())).append(",");
                writer.append(escapeCsv(getRegistrationDate(event, entrant))).append("\n");
            }


            writer.flush();
            writer.close();


            Log.d("CSV_EXPORT", "Accepted entrants CSV exported to: " + file.getAbsolutePath());
            return true;


        } catch (IOException e) {
            Log.e("CSV_EXPORT", "Error exporting CSV: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }


    /**
     * Makes a filename safe by removing bad characters
     * Replaces spaces and special chars with underscores
     */




    private static String sanitizeFileName(String fileName) {
        return fileName.replaceAll("[^a-zA-Z0-9._-]", "_");
    }


    /**
     * Formats text for CSV - adds quotes if needed
     * Handles commas, quotes, and newlines in the text
     */
    private static String escapeCsv(String value) {
        if (value == null || value.isEmpty()) return "";
        // Escape quotes and wrap in quotes if contains comma or special chars
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            value = value.replace("\"", "\"\"");
            return "\"" + value + "\"";
        }
        return value;
    }
    /**
     * Gets the status of an entrant
     * Currently just returns "Accepted" for everyone
     */


    private static String getEntrantStatus(Event event, Profile entrant) {
        if (event.getAcceptedEntrants().contains(entrant.getProfileId())) return "Accepted";
        if (event.getInvitedList().contains(entrant.getProfileId())) return "Invited";
        if (event.getWaitingList().contains(entrant.getProfileId())) return "Waiting";
        return "Unknown";
    }




    private static String getRegistrationDate(Event event, Profile entrant) {


        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(event.getRegistrationStart());
    }
}
