package com.seismic.p;

import themidibus.*;
import processing.serial.*;

import processing.core.PApplet;

public class SerialMIDITrigger extends PApplet {

    public static void main(String[] args) {
        PAppletRunner.run(new SerialMIDITrigger());
    }

    MidiBus myBus; // The MidiBus
    Serial myPort;  // Create object from SeismicSerial class
    String val;      // Data received from the serial port

    int lastKickChannel;
    int lastSnareChannel;
    int channel;

    String lastOn;
    String lastOff;
    int handleValue;

    public void settings() {
        size(700, 700);
    }

    public void setup() {
        frameRate(1000);

        String[] ports = Serial.list();
        for (int i = 0; i < ports.length; i++) {
            System.out.println(ports[i] + " " + i);
        }
        myPort = new Serial(this, "/dev/cu.usbserial-16TNB297", 9600);
        myBus = new MidiBus(this, -1, "IAC Bus 2");
    }

    int getChannel(String trigger, int handle) {
        if (trigger.equals("KICK")) {
            lastKickChannel = (int) constrain(map(handle, 400, 700, 2, 0), 0, 2);
            return lastKickChannel;
        } else {
            lastSnareChannel = (int) constrain(map(handle, 400, 700, 5, 3), 3, 5);
            return lastSnareChannel;
        }
    }

    int getLastChannel(String trigger) {
        if (trigger.equals("KICK")) {
            return lastKickChannel;
        } else {
            return lastSnareChannel;
        }
    }

    public void draw() {
        background(0);
        if ( myPort.available() > 0) {  // If data is available,
            val = myPort.readStringUntil(10);        // read it and store it in val
            if (val != null) {
                String s = val.trim();
                String[] vals = s.split(",");
                String onOff = vals[0];
                String trigger = vals[1];

                if (onOff.equals("ON")) {
                    int triggerValue = Integer.parseInt(vals[2]);
                    handleValue = Integer.parseInt(vals[3]);
                    channel = getChannel(trigger, handleValue);

                    int velocity = constrain((int) map(triggerValue, 0, 900, 0, 127), 0, 127);
                    myBus.sendNoteOn(channel, 60, velocity);
                    lastOn = s;
                } else if (onOff.equals("OFF")) {
                    myBus.sendNoteOff(getLastChannel(trigger), 60, 0);
                    lastOff = s;
                } else {
                    println("Unknown message: " + val);
                }
            }
        }

        textSize(23);
        if (lastOn != null) {
            text("NOTE ON: " + lastOn + " ch: " + channel, 10, 30);
        }
        if (lastOff != null) {
            text("NOTE OFF: " + lastOff + " ch: " + channel, 10, 60);
        }

        text("HANDLE: " + handleValue, 10, 90);
    }
}
