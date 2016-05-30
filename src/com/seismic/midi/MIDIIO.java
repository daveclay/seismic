package com.seismic.midi;

import javax.sound.midi.MidiMessage;

public interface MIDIIO {
    /**
     * List all installed MIDI devices. The index, name and type (input/output/unavailable) of each devices will be
     * indicated.
     */
    static void list() {
        String[] availableInputs = StupidMonkeyMIDI.availableInputs();
        String[] availableOutputs = StupidMonkeyMIDI.availableOutputs();
        String[] unavailable = StupidMonkeyMIDI.unavailableDevices();

        if (availableInputs.length == 0 && availableOutputs.length == 0 && unavailable.length == 0) return;

        System.out.println("\nAvailable MIDI Devices:");
        if (availableInputs.length != 0) {
            System.out.println("----------Input----------");
            for (int i = 0; i < availableInputs.length; i++)
                System.out.println("[" + i + "] \"" + availableInputs[i] + "\"");
        }
        if (availableOutputs.length != 0) {
            System.out.println("----------Output----------");
            for (int i = 0; i < availableOutputs.length; i++)
                System.out.println("[" + i + "] \"" + availableOutputs[i] + "\"");
        }
        if (unavailable.length != 0) {
            System.out.println("----------Unavailable----------");
            for (int i = 0; i < unavailable.length; i++) System.out.println("[" + i + "] \"" + unavailable[i] + "\"");
        }
    }

    String[] attachedInputs();

    String[] attachedOutputs();

    boolean addInput(int deviceNum);

    boolean removeInput(int deviceNum);

    boolean addInput(String deviceName);

    boolean removeInput(String deviceName);

    boolean addOutput(int deviceNum);

    boolean removeOutput(int deviceNum);

    boolean addOutput(String deviceName);

    boolean removeOutput(String deviceName);

    void clearInputs();

    void clearOutputs();

    void clearAll();

    void sendMessage(byte[] data);

    void sendMessage(int status);

    void sendMessage(int status, int data);

    void sendMessage(int status, int data1, int data2);

    void sendMessage(int command, int channel, int data1, int data2);

    void sendMessage(MidiMessage message);

    void sendNoteOn(int channel, int pitch, int velocity);

    void sendNoteOn(Note note);

    void sendNoteOff(int channel, int pitch, int velocity);

    void sendNoteOff(Note note);

    void sendControllerChange(int channel, int number, int value);

    void sendControllerChange(ControlChange change);

    boolean addMidiListener(MidiListener listener);

    boolean removeMidiListener(MidiListener listener);

    String getBusName();

    void close();
}
