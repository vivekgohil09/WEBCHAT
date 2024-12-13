URL: https://demo-deployment-latest-3pm1.onrender.com/


User Flow:

Users visit the website and are presented with a room entry form.
They enter a room name and join a chat.
Once inside, they can send and receive messages in real time.
They can also share files (images, videos, etc.) and the messages will be displayed in the chat window.
Real-Time Communication:

The chat application uses WebSocket to handle real-time message exchanges. When a user sends a message, it is immediately broadcasted to all other participants in the same room without needing to refresh the page.
Thymeleaf for Dynamic Content:

The chat interface is rendered dynamically using Thymeleaf, making the application server-side rendered while maintaining the ability to dynamically update the UI based on server-side state changes (such as new messages).
Future Enhancements
User Authentication: Implement user authentication so that users can log in and have a persistent identity across multiple sessions.
Message History: Store and retrieve chat history from a database (e.g., MySQL or MongoDB) so users can view previous messages.
File Storage Integration: Enhance the file sharing feature by integrating a cloud storage solution like AWS S3 or Google Cloud Storage.
Private Messaging: Implement a feature to allow users to send private messages to each other within the chat room.
Contributing
If you'd like to contribute to this project, feel free to fork the repository and submit a pull request. Any contributions to improve the project are highly appreciated!

License
This project is licensed under the MIT License - see the LICENSE file for details
