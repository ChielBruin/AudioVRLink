# AudioVRLink
An Android application for controlling VR audio games.

This android application uses the sensors of the phone to calculate a forwards facing direction vector.
This vector can be used by a receiver to control the direction a player is facing in (for example) a game.
This app is designed for usage with the audio-only VR game Legend of Iris* and is therefore only built as a controller and not a remote screen.
A movement speed is also added to the send data to make this app capable of controlling the entire game.

In the future it should be possible for the connected game to send audio back to this app to make an audio based VR game (in contrast to the Rift/Vive) a wireless experience.

## Connection
The app connects to a receiver on port 6000 on an IP address entered by the user.
It isalso possible for the user to connect using a 4-digit connection code.
This code allows to connect to a receiver that is on the same network as the phone.
The first three digits are the last part of the IP address of the receiver (with additional zeros if the value is < 100).
A checksum is added as the fourth digit to check if the phone is on the same network as the receiving node.
for an IP address 'a.b.c.d' the first part of the code is equal to d and the checksum is calculated with a simple formula: 
checksum = (a + 3b + c) % 10.
For example the IP address '192.193.13.123' gives connect code '1234'

## Data format
The format of the data produced by this app is a message containing 4 lines:
*dir.x
*dir.y
*dir.z
*moveSpeed
In this message 'dir' is the direction vector that follows the Unity Vector3 layout and values. 
So a forward facing vector is equal to the unity equivalent 'Vector3.forward'.
The movement speed is a float value that can have three values: -1, 0 and 1. 
These values represent backward, idle and forward movement respectively.

\* Interested in the game? Check it out: 'https://github.com/ChielBruin/legend-of-iris' At the moment you need to checkout the branch AudioVRLink
