# Entrant and Organizer User Stories – Prerequisites and Implementation Notes  
*(Assigned to: `amritpa6` – Based on EO branch structure and current app setup)*  

---

## **US 01.01.04**  
**As an entrant, I want to filter events based on my interests and availability.** *(Issue #4)*  

**Prerequisites:**  
- Event list fragment with working **RecyclerView / ListView**  
- Firebase Firestore setup with event categories, date, and tags  
- ViewModel logic for filtering events by interest, time, or category  
- UI components for search and filter options  

**Implementation Notes:**  
- Use checkboxes or dropdowns for filters (e.g., “Music”, “Sports”, “Weekend”)  
- Query Firestore dynamically for real-time filtering  
- Keep filtered list scrollable and easy to reset  

---

## **US 02.04.01**  
**As an organizer, I want to upload an event poster to the event details page to provide visual information to entrants.** *(Issue #26)*  

**Prerequisites:**  
- Event creation fragment with image upload field  
- Firebase Storage configured for storing posters  
- Event model includes a `posterUrl` attribute  

**Implementation Notes:**  
- Add **“Upload Poster”** button → opens gallery/camera picker  
- Store image in Firebase Storage and save URL in Firestore  
- Display uploaded image preview on the event creation screen  

---

## **US 02.04.02**  
**As an organizer, I want to update an event poster to provide visual information to entrants.** *(Issue #27)*  

**Prerequisites:**  
- Upload logic from US 02.04.01 already implemented  
- Firebase Storage overwrite access enabled  
- UI setup for preview and edit button  

**Implementation Notes:**  
- Add **“Edit Poster”** button near preview  
- Delete old image in Firebase Storage and upload new one  
- Show success message “Poster updated successfully.”  

---

## **US 02.05.01**  
**As an organizer, I want to send a notification to chosen entrants to sign up for events (the ‘win’ notification).** *(Issue #28)*  

**Prerequisites:**  
- Firebase Cloud Messaging (FCM) or in-app notification setup  
- Notification model with `senderId`, `receiverId`, `eventId`, `message`, and `timestamp`  
- Link between selected entrants and event data  

**Implementation Notes:**  
- Trigger “winner” notification after drawing selected entrants  
- Save each notification in Firestore logs for admin viewing  
- Display success alert/toast message for entrants  

---

## **Additional Related Entrant Stories**

### **US 01.04.03 – Opt out of notifications**  
*Relation:* Connects to your notification system (US 02.05.01).  
Entrants need a toggle in the profile fragment to disable notifications.  
Notifications must check this setting before sending messages.  

### **US 01.05.05 – Lottery guidelines**  
*Relation:* Connects to your filter/search feature (US 01.01.04).  
Entrants might view guidelines before joining events. Add a small “Info” button or popup.  

### **US 01.02.02 – Update profile info**  
*Relation:* Affects your notification and event logic.  
Profile updates ensure correct entrant data in events and messages.  

### **US 01.02.01 – Provide personal info**  
*Relation:* Base requirement for all entrant actions.  
Your features depend on entrants having profiles linked with IDs and contact info.  

---

## **Generally**  
- Firebase setup needed for users, events, posters, and notifications  
- ViewModel to share entrant/organizer data across fragments  
- RecyclerView / ListView for event, poster, and notification displays  
- Maintain consistent UI flow with admin and organizer modules  
- Ensure entrant data syncs correctly with event and notification logic  
