package org.example;

import java.awt.*;
import java.awt.event.KeyEvent;

public class InputHandler implements Runnable {
    private final StringBuilder currentInput = new StringBuilder(); // To store the current sequence of digits

    @Override
    public void run() {
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
            @Override
            public boolean dispatchKeyEvent(KeyEvent keyEvent) {
                if (keyEvent.getID() == KeyEvent.KEY_TYPED) {
                    char keyChar = keyEvent.getKeyChar();

                    // Check if the key is a digit (for entering numbers)
                    if (Character.isDigit(keyChar)) {
                        // Add digit to the current input
                        currentInput.append(keyChar);

                    } else if (keyChar == 's') {
                        // If 's' is pressed, add 's' to the event queue
                        GameLoop.events.add((int) 's');
                        currentInput.setLength(0); // Reset input after adding
                    } else if (keyChar == 'm') {
                        // If 'm' is pressed, add 'm' to the event queue
                        GameLoop.events.add((int) 'm');
                        currentInput.setLength(0); // Reset input after adding
                    } else if (keyChar == '\n') { // Assume Enter key finalizes the number
                        if (!currentInput.isEmpty()) {
                            try {
                                // Parse the collected digits as a number and add to events
                                int enteredValue = Integer.parseInt(currentInput.toString());
                                GameLoop.events.add(enteredValue);  // Add the number to events
                            } catch (NumberFormatException e) {
                                // Handle invalid input
                                System.out.println("Invalid input. Please enter a valid number.");
                            }
                            currentInput.setLength(0); // Reset input buffer after processing
                        }
                    } else {
                        // Handle invalid keys (optional)
                        System.out.println("Invalid key pressed: " + keyChar);
                    }
                }
                return true; // Return true to indicate the key event has been handled
            }
        });
    }
}
