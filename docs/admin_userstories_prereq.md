# Entrant User Stories – Prerequisites and Implementation Notes  
*(Based on EO branch structure and current admin fragment setup)*  






---

###  US 01.05.02  
**As an entrant, I want to be able to accept the invitation to register/sign up when chosen to participate in an event. (Issue #13)**  

**Prerequisites:**  
- Firebase connection for event invitations (e.g., `Invitations` collection).  
- Data model class for `Invitation` with fields: eventId, entrantId, status (pending/accepted/declined).  
- ViewModel method to update invitation status when entrant accepts.  
- Fragment or dialog UI for entrant to view and accept/decline invitations.  

**Implementation Notes:**  
- Show invitations on entrant dashboard or notification fragment.  
- Upon “Accept”, link entrant to the event entry in Firebase.  
- Ensure confirmation message or navigation to event details page.





---

### US 01.04.02  
**As an entrant, I want to receive notification of when I am not chosen on the app (when I “lose” the lottery). (Issue #10)**  

**Prerequisites:**  
- Notification class and Firebase logic for lottery results.  
- Event or LotteryResult listener to trigger notification when entrant not selected.  
- Fragment or push notification to show “Not Selected” message.  

**Implementation Notes:**  
- Notifications can be handled using Firebase Cloud Messaging or a local in-app notification system.  
- Ensure notifications respect entrant’s opt-out setting from US 01.04.03.  








---

###  US 01.02.04  
**As an entrant, I want to delete my profile if I no longer wish to use the app. (Issue #8)**  

**Prerequisites:**  
- Profile fragment must include a “Delete Account” button.  
- Firebase user deletion method (auth + Firestore entry removal).  
- Confirmation dialog before deletion.  

**Implementation Notes:**  
- Use Firebase Authentication’s `delete()` for the current user.  
- After deletion, redirect to login or welcome screen.  





---

###  US 01.02.03  
**As an entrant, I want to have a history of events I have registered for, whether I was selected or not. (Issue #7)**  

**Prerequisites:**  
- Database structure to store entrant-event relationships (e.g., `registrations` collection).  
- Model class for Registration with fields: eventId, entrantId, status (selected / not selected).  
- Fragment or RecyclerView to display event history.  

**Implementation Notes:**  
- ViewModel retrieves list of past events from Firebase.  
- Add filters or tabs for “Selected” vs “Not Selected”.  






---

### US 01.04.01  
**As an entrant, I want to receive notification when I am chosen to participate from the waiting list (when I “win” the lottery). (Issue #9)**  

**Prerequisites:**  
- Notification class and Firebase trigger when entrant’s lottery status changes to “Selected”.  
- UI handling for displaying success notification or dialog.  

**Implementation Notes:**  
- Similar logic as US 01.04.02 but for “Selected” case.  
- Should also create invitation record to allow entrant to accept/decline participation (linked to US 01.05.02).  





---

###  General Notes
- All entrant-related fragments need connection through a **ViewModel** to pass entrant info across screens.  
- **Event** and **Notification** model classes should be implemented in `/model/`.  
- **Firebase setup** required for entrants collection, events, and invitations.  
- Check EO branch for admin fragments — ensure consistent navigation and naming.  
- After implementing, verify navigation between Login ↔ Entrant Dashboard ↔ Profile ↔ Notifications.  

---

 *Added by [Olarinde] for entrant user story coordination and prerequisite tracking.*
