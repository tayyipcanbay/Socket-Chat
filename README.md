# Socket

Our server, featuring threaded architecture, adeptly manages incoming connections, user interactions, and communication—ensuring optimal performance through parallel processing.

## Server

The server handles incoming connections from clients, manages a list of connected users, and facilitates communication between them.

### Usage

1. Compile the server code:

    ```bash
    javac Server.java
    ```

2. Run the server:

    ```bash
    java Server
    ```

3. Enter a username when prompted.

### Commands

- `/online`: View a list of online users.
- `/help`: Display a list of available commands.
- `/kick <username> <reason>`: Kick a user from the chat (admin-only).
- `/admin`: Display an admin message (admin-only).
- `/quit`: Disconnect from the chat.

## Client

The client connects to the server, allows users to choose a username, and enables communication through the console.

### Usage

1. Compile the client code:

    ```bash
    javac Client.java
    ```

2. Run the client:

    ```bash
    java Client
    ```

3. Enter a username when prompted.

### Commands

- Type messages to send them to the chat.
- `/online`: View a list of online users.
- `/quit`: Disconnect from the chat.

## Notes

- Private messages can be sent using the `/private <username> <message>` command.
- The server broadcasts messages to all connected clients.
- The server provides basic admin functionality with the `/admin` and `/kick` commands.
