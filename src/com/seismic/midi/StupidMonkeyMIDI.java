package com.seismic.midi;

import javax.sound.midi.*;
import java.util.ArrayList;
import java.util.Formatter;

public class StupidMonkeyMIDI implements MIDIIO {

    static MidiDevice.Info[] availableDevices;

    String busName;
    boolean sendTimestamps;

    ArrayList<InputDeviceContainer> inputDevicesContainers;
    ArrayList<OutputDeviceContainer> outputDeviceContainers;

    ArrayList<MidiListener> listeners;


    /**
     * Perfoms the initialisation of new MidiBus objects, is private for a reason, and is only ever called within the
     * constructors. This method exists only for the purpose of cleaner and easier to maintain code.
     * Creates a new (hopefully/probably) unique busName value for new MidiBus objects that weren't given one.
     * If two MidiBus object were to have the same name, this would be bad, but not fatal, so there's no point in
     * spending too much time worrying about it.
     */
    public StupidMonkeyMIDI(String busName) {
        if (busName == null) {
            generateBusName();
        } else {
            this.busName = busName;
        }

        inputDevicesContainers = new ArrayList();
        outputDeviceContainers = new ArrayList();

        listeners = new ArrayList();

        sendTimestamps = true;
    }

	/* -- Input/Output Handling -- */

    /**
     * Returns the names of all the attached input devices.
     *
     * @return the names of the attached inputs.
     * @see #attachedOutputs()
     */
    @Override
    public String[] attachedInputs() {
        MidiDevice.Info[] deviceInfos = attachedInputsMidiDeviceInfo();
        String[] devices = new String[deviceInfos.length];

        for (int i = 0; i < deviceInfos.length; i++) {
            devices[i] = deviceInfos[i].getName();
        }

        return devices;
    }

    /**
     * Returns the names of all the attached output devices.
     *
     * @return the names of the attached outputs.
     * @see #attachedInputs()
     */
    @Override
    public String[] attachedOutputs() {
        MidiDevice.Info[] deviceInfos = attachedOutputsMidiDeviceInfo();
        String[] devices = new String[deviceInfos.length];

        for (int i = 0; i < deviceInfos.length; i++) {
            devices[i] = deviceInfos[i].getName();
        }

        return devices;
    }

    /**
     * Returns the MidiDevice.Info of all the attached input devices.
     *
     * @return the MidiDevice.Info of the attached inputs.
     */
    MidiDevice.Info[] attachedInputsMidiDeviceInfo() {
        MidiDevice.Info[] devices = new MidiDevice.Info[inputDevicesContainers.size()];

        for (int i = 0; i < inputDevicesContainers.size(); i++) {
            devices[i] = inputDevicesContainers.get(i).info;
        }

        return devices;
    }

    /**
     * Returns the MidiDevice.Info of all the attached output devices.
     *
     * @return the MidiDevice.Info of the attached outputs.
     */
    MidiDevice.Info[] attachedOutputsMidiDeviceInfo() {
        MidiDevice.Info[] devices = new MidiDevice.Info[outputDeviceContainers.size()];

        for (int i = 0; i < outputDeviceContainers.size(); i++) {
            devices[i] = outputDeviceContainers.get(i).info;
        }

        return devices;
    }

    /**
     * Adds a new MIDI input device specified by the index deviceNum. If the MIDI input device has already been added, it will not be added again.
     *
     * @param deviceNum the index of the MIDI input device to be added.
     * @return true if and only if the input device was successfully added.
     * @see #addInput(String device_name)
     * @see #list()
     */
    @Override
    public boolean addInput(int deviceNum) {
        if (deviceNum == -1) return false;

        MidiDevice.Info[] devices = availableInputsMidiDeviceInfo();

        if (deviceNum >= devices.length || deviceNum < 0) {
            System.err.println("\nThe MidiBus Warning: The chosen input device numbered [" + deviceNum + "] was not added because it doesn't exist");
            return false;
        }

        return addInput(devices[deviceNum]);
    }


    /**
     * Removes the MIDI input device specified by the index deviceNum.
     *
     * @param deviceNum the index of the MIDI input device to be removed.
     * @return true if and only if the input device was successfully removed.
     * @see #removeInput(String device_name)
     * @see #attachedInputs()
     */
    @Override
    public synchronized boolean removeInput(int deviceNum) {
        try {
            InputDeviceContainer container = inputDevicesContainers.get(deviceNum);

            inputDevicesContainers.remove(container);

            container.transmitter.close();
            container.receiver.close();

            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Adds a new MIDI input device specified by the name device_name. If the MIDI input device has already been added, it will not be added again.
     * <p>
     * If two or more MIDI inputs have the same name, whichever appears first when {@link #list()} is called will be added. If this behavior is problematic use {@link #addInput(int deviceNum)} instead.
     *
     * @param deviceName the name of the MIDI input device to be added.
     * @return true if and only if the input device was successfully added.
     * @see #addInput(int deviceNum)
     * @see #list()
     */
    @Override
    public boolean addInput(String deviceName) {
        if (deviceName.equals("")) return false;

        MidiDevice.Info[] devices = availableInputsMidiDeviceInfo();

        for (int i = 0; i < devices.length; i++) {
            if (devices[i].getName().equals(deviceName)) return addInput(devices[i]);
        }

        System.err.println("\nThe MidiBus Warning: No available input MIDI devices named: \"" + deviceName + "\" were found");
        return false;
    }

    /**
     * Removes the MIDI input device specified by the name deviceName.
     * <p>
     * If two or more attached MIDI inputs have the same name, whichever appears first when {@link #attachedInputs()}
     * is called will be removed. If this behavior is problematic use {@link #removeInput(int deviceNum)} instead.
     *
     * @param deviceName the name of the MIDI input device to be removed.
     * @return true if and only if the input device was successfully removed.
     * @see #removeInput(int deviceNum)
     * @see #attachedInputs()
     */
    @Override
    public synchronized boolean removeInput(String deviceName) {
        for (InputDeviceContainer container : inputDevicesContainers) {
            if (container.info.getName().equals(deviceName)) {
                inputDevicesContainers.remove(container);

                container.transmitter.close();
                container.receiver.close();

                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new MIDI input device specified by the MidiDevice.Info device_info. If the MIDI input device has already been added, it will not be added again.
     *
     * @param deviceInfo the MidiDevice.Info of the MIDI input device to be added.
     * @return true if and only if the input device was successfully added.
     */
    synchronized boolean addInput(MidiDevice.Info deviceInfo) {
        try {
            MidiDevice newDevice = MidiSystem.getMidiDevice(deviceInfo);

            if (newDevice.getMaxTransmitters() == 0) {
                System.err.println("\nThe MidiBus Warning: The chosen input device \"" + deviceInfo.getName() + "\" was not added because it is output only");
                return false;
            }

            for (InputDeviceContainer container : inputDevicesContainers) {
                if (deviceInfo.getName().equals(container.info.getName())) return false;
            }

            if (!newDevice.isOpen()) newDevice.open();

            MReceiver receiver = new MReceiver();
            Transmitter transmitter = newDevice.getTransmitter();
            transmitter.setReceiver(receiver);

            InputDeviceContainer newContainer = new InputDeviceContainer(newDevice);
            newContainer.transmitter = transmitter;
            newContainer.receiver = receiver;

            inputDevicesContainers.add(newContainer);

            return true;
        } catch (MidiUnavailableException e) {
            System.err.println("\nThe MidiBus Warning: The chosen input device \"" + deviceInfo.getName() + "\" was not added because it is unavailable");
            return false;
        }
    }

    /**
     * Adds a new MIDI output device specified by the index deviceNum. If the MIDI output device has already been added, it will not be added again.
     *
     * @param deviceNum the index of the MIDI output device to be added.
     * @return true if and only if the output device was successfully added.
     * @see #addOutput(String deviceName)
     * @see #list()
     */
    @Override
    public boolean addOutput(int deviceNum) {
        if (deviceNum == -1) return false;

        MidiDevice.Info[] devices = availableOutputsMidiDeviceInfo();

        if (deviceNum >= devices.length || deviceNum < 0) {
            System.err.println("\nThe MidiBus Warning: The chosen output device numbered [" + deviceNum + "] was not added because it doesn't exist");
            return false;
        }

        return addOutput(devices[deviceNum]);
    }

    /**
     * Removes the MIDI output device specified by the index deviceNum.
     *
     * @param deviceNum the index of the MIDI output device to be removed.
     * @return true if and only if the output device was successfully removed.
     * @see #removeInput(String deviceName)
     * @see #attachedOutputs()
     */
    @Override
    public synchronized boolean removeOutput(int deviceNum) {
        try {
            OutputDeviceContainer container = outputDeviceContainers.get(deviceNum);

            outputDeviceContainers.remove(container);

            container.receiver.close();

            return true;
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Adds a new MIDI output device specified by the name deviceName. If the MIDI output device has already been added, it will not be added again.
     * <p>
     * If two or more MIDI outputs have the same name, whichever appears first when {@link #list()} is called will be added. If this behavior is problematic use {@link #addOutput(int deviceNum)} instead.
     *
     * @param deviceName the name of the MIDI output device to be added.
     * @return true if and only if the output device was successfully added.
     * @see #addOutput(int deviceNum)
     * @see #list()
     */
    @Override
    public boolean addOutput(String deviceName) {
        if (deviceName.equals("")) return false;

        MidiDevice.Info[] devices = availableOutputsMidiDeviceInfo();

        for (int i = 0; i < devices.length; i++) {
            if (devices[i].getName().equals(deviceName)) return addOutput(devices[i]);
        }

        System.err.println("\nThe MidiBus Warning: No available input MIDI devices named: \"" + deviceName + "\" were found");
        return false;
    }

    /**
     * Removes the MIDI output device specified by the name deviceName.
     * <p>
     * If two or more attached MIDI outputs have the same name, whichever appears first when {@link #attachedOutputs()} is called will be removed. If this behavior is problematic use {@link #removeOutput(int deviceNum)} instead.
     *
     * @param deviceName the name of the MIDI output device to be removed.
     * @return true if and only if the output device was successfully removed.
     * @see #removeOutput(int deviceNum)
     * @see #attachedOutputs()
     */
    @Override
    public synchronized boolean removeOutput(String deviceName) {
        for (OutputDeviceContainer container : outputDeviceContainers) {
            if (container.info.getName().equals(deviceName)) {
                outputDeviceContainers.remove(container);

                container.receiver.close();

                return true;
            }
        }
        return false;
    }

    /**
     * Adds a new MIDI output device specified by the MidiDevice.Info deviceInfo. If the MIDI output device has already been added, it will not be added again.
     *
     * @param deviceInfo the MidiDevice.Info of the MIDI output device to be added.
     * @return true if and only if the input device was successfully added.
     */
    synchronized boolean addOutput(MidiDevice.Info deviceInfo) {
        try {
            MidiDevice newDevice = MidiSystem.getMidiDevice(deviceInfo);

            if (newDevice.getMaxReceivers() == 0) {
                System.err.println("\nThe MidiBus Warning: The chosen output device \"" + deviceInfo.getName() + "\" was not added because it is input only");
                return false;
            }

            for (OutputDeviceContainer container : outputDeviceContainers) {
                if (deviceInfo.getName().equals(container.info.getName())) return false;
            }

            if (!newDevice.isOpen()) newDevice.open();

            OutputDeviceContainer newContainer = new OutputDeviceContainer(newDevice);
            newContainer.receiver = newDevice.getReceiver();

            outputDeviceContainers.add(newContainer);

            return true;
        } catch (MidiUnavailableException e) {
            System.err.println("\nThe MidiBus Warning: The chosen output device \"" + deviceInfo.getName() + "\" was not added because it is unavailable");
            return false;
        }
    }

    /**
     * Closes, clears and disposes of all input related Transmitters and Receivers.
     *
     * @see #clearOutputs()
     * @see #clearAll()
     */
    @Override
    public synchronized void clearInputs() {
        //We are purposefully not closing devices here, because in some cases that will be slow, and we might want later
        //Also it's broken on MAC
        try {
            for (InputDeviceContainer container : inputDevicesContainers) {
                container.transmitter.close();
                container.receiver.close();
            }
        } catch (Exception e) {
            System.err.println("The MidiBus Warning: Unexpected error during clearInputs()");
        }

        inputDevicesContainers.clear();
    }

    /**
     * Closes, clears and disposes of all output related Receivers.
     *
     * @see #clearInputs()
     * @see #clearAll()
     */
    @Override
    public synchronized void clearOutputs() {
        //We are purposefully not closing devices here, because in some cases that will be slow, and we might want later
        //Also it's broken on MAC
        try {
            for (OutputDeviceContainer container : outputDeviceContainers) {
                container.receiver.close();
            }
        } catch (Exception e) {
            System.err.println("The MidiBus Warning: Unexpected error during clearOutputs()");
        }

        outputDeviceContainers.clear();
    }

    /**
     * Closes, clears and disposes of all input and output related Transmitters and Receivers.
     *
     * @see #clearInputs()
     * @see #clearOutputs()
     */
    @Override
    public void clearAll() {
        clearInputs();
        clearOutputs();
    }

    /**
     * Closes all MidiDevices, should only be called when closing the application, will interrupt all MIDI I/O. Call publicly from stop(), close() or dispose()
     *
     * @see #clearOutputs()
     * @see #clearInputs()
     * @see #clearAll()
     */
    private void closeAllMidiDevices() {
        if (availableDevices == null) findMidiDevices();
        MidiDevice device;

        for (int i = 0; i < availableDevices.length; i++) {
            try {
                device = MidiSystem.getMidiDevice(availableDevices[i]);
                if (device.isOpen()) device.close();
            } catch (MidiUnavailableException e) {
                //Device wasn't available, which is fine since we wanted to close it anyways
            }
        }

    }

	/* -- MIDI Out -- */

    /**
     * Sends a MIDI message with an unspecified number of bytes. The first byte should be always be the status byte. If the message is a Meta message of a System Exclusive message it can have more than 3 byte, otherwise all extra bytes will be dropped.
     *
     * @param data the bytes of the MIDI message.
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendMessage(byte[] data) {
        if ((data[0] & 0xFF) == MetaMessage.META) {
            MetaMessage message = new MetaMessage();
            try {
                byte[] payload = new byte[data.length - 2];
                System.arraycopy(data, 2, payload, 0, data.length - 2);
                message.setMessage((data[1] & 0xFF), payload, data.length - 2);
                sendMessage(message);
            } catch (InvalidMidiDataException e) {
                System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
            }
        } else if ((data[0] & 0xFF) == SysexMessage.SYSTEM_EXCLUSIVE || (data[0] & 0xFF) == SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE) {
            SysexMessage message = new SysexMessage();
            try {
                message.setMessage(data, data.length);
                sendMessage(message);
            } catch (InvalidMidiDataException e) {
                System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
            }
        } else {
            ShortMessage message = new ShortMessage();
            try {
                if (data.length > 2)
                    message.setMessage((data[0] & 0xFF), (data[1] & 0xFF), (data[2] & 0xFF));
                else if (data.length > 1)
                    message.setMessage((data[0] & 0xFF), (data[1] & 0xFF), 0);
                else message.setMessage((data[0] & 0xFF));
                sendMessage(message);
            } catch (InvalidMidiDataException e) {
                System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
            }
        }
    }

    /**
     * Sends a MIDI message that takes no data bytes.
     *
     * @param status the status byte
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendMessage(int status) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(status);
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
            System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
        }
    }

    /**
     * Sends a MIDI message that takes only one data byte. If the message does not take data, the data byte is ignored.
     *
     * @param status the status byte
     * @param data   the data byte
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendMessage(int status, int data) {
        sendMessage(status, data, 0);
    }

    /**
     * Sends a MIDI message that takes one or two data bytes. If the message takes only one data byte, the second data byte is ignored; if the message does not take any data bytes, both data bytes are ignored.
     *
     * @param status the status byte.
     * @param data1  the first data byte.
     * @param data2  the second data byte.
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendMessage(int status, int data1, int data2) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(status, data1, data2);
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
        }
    }

    /**
     * Sends a channel message which takes up to two data bytes. If the message only takes one data byte, the second data byte is ignored; if the message does not take any data bytes, both data bytes are ignored.
     *
     * @param command the MIDI command represented by this message.
     * @param channel the channel associated with the message.
     * @param data1   the first data byte.
     * @param data2   the second data byte.
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendMessage(int command, int channel, int data1, int data2) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(command, channel, data1, data2);
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
        }
    }

    /**
     * Sends a MidiMessage object.
     *
     * @param message the MidiMessage.
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public synchronized void sendMessage(MidiMessage message) {
        for (OutputDeviceContainer container : outputDeviceContainers) {
            if (sendTimestamps) container.receiver.send(message, System.currentTimeMillis());
            else container.receiver.send(message, 0);
        }
    }

    /**
     * Sends a NoteOn message to a channel with the specified pitch and velocity.
     *
     * @param channel  the channel associated with the message.
     * @param pitch    the pitch associated with the message.
     * @param velocity the velocity associated with the message.
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendNoteOn(int channel, int pitch, int velocity) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_ON, constrain(channel, 0, 15), constrain(pitch, 0, 127), constrain(velocity, 0, 127));
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
        }
    }

    /**
     * Sends a NoteOn message to a channel with the specified Note.
     *
     * @param note the Note object for the message.
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendNoteOn(Note note) {
        sendNoteOn(note.channel(), note.pitch(), note.velocity());
    }

    /**
     * Sends a NoteOff message to a channel with the specified pitch and velocity.
     *
     * @param channel  the channel associated with the message.
     * @param pitch    the pitch associated with the message.
     * @param velocity the velocity associated with the message.
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendNoteOff(int channel, int pitch, int velocity) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.NOTE_OFF, constrain(channel, 0, 15), constrain(pitch, 0, 127), constrain(velocity, 0, 127));
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
        }
    }

    /**
     * Sends a NoteOff message to a channel with the specified Note.
     *
     * @param note the Note object for the message.
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendControllerChange(int channel, int number, int value)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendNoteOff(Note note) {
        sendNoteOff(note.channel, note.pitch(), note.velocity());
    }

    /**
     * Sends a ControllerChange message to a channel with the specified number and value.
     *
     * @param channel the channel associated with the message.
     * @param number  the number associated with the message.
     * @param value   the value associated with the message.
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(ControlChange change)
     */
    @Override
    public void sendControllerChange(int channel, int number, int value) {
        ShortMessage message = new ShortMessage();
        try {
            message.setMessage(ShortMessage.CONTROL_CHANGE, constrain(channel, 0, 15), constrain(number, 0, 127), constrain(value, 0, 127));
            sendMessage(message);
        } catch (InvalidMidiDataException e) {
            System.err.println("\nThe MidiBus Warning: Message not sent, invalid MIDI data");
        }
    }

    /**
     * Sends a ControllerChange message to a channel with the specified ControlChange.
     *
     * @param change the ControlChange object for the message.
     * @see #sendMessage(byte[] data)
     * @see #sendMessage(int status)
     * @see #sendMessage(int status, int data)
     * @see #sendMessage(int status, int data1, int data2)
     * @see #sendMessage(int command, int channel, int data1, int data2)
     * @see #sendMessage(MidiMessage message)
     * @see #sendNoteOn(int channel, int pitch, int velocity)
     * @see #sendNoteOn(Note note)
     * @see #sendNoteOff(int channel, int pitch, int velocity)
     * @see #sendNoteOff(Note note)
     * @see #sendControllerChange(int channel, int number, int value)
     */
    @Override
    public void sendControllerChange(ControlChange change) {
        sendControllerChange(change.channel(), change.number(), change.value());
    }

	/* -- MIDI In -- */

    /**
     * Notifies all types of listeners of a new MIDI message from one of the MIDI input devices.
     *
     * @param message the new inbound MidiMessage.
     */
    private void notifyListeners(MidiMessage message, long timeStamp) {
        byte[] data = message.getMessage();

        for (MidiListener listener : listeners) {
            listener.midiMessage(message, timeStamp);

            if ((data[0] & 0xF0) == ShortMessage.NOTE_ON) {
                listener.noteOn((data[0] & 0x0F), (data[1] & 0xFF), (data[2] & 0xFF));
            } else if ((data[0] & 0xF0) == ShortMessage.NOTE_OFF) {
                listener.noteOff((data[0] & 0x0F), (data[1] & 0xFF), (data[2] & 0xFF));
            } else if ((data[0] & 0xF0) == ShortMessage.CONTROL_CHANGE) {
                listener.controllerChange((data[0] & 0x0F), (data[1] & 0xFF), (data[2] & 0xFF));
            }

            if ((data[0] & 0xF0) == ShortMessage.NOTE_ON) {
                listener.noteOn(new Note((data[0] & 0x0F), (data[1] & 0xFF), (data[2] & 0xFF)));
            } else if ((data[0] & 0xF0) == ShortMessage.NOTE_OFF) {
                listener.noteOff(new Note((data[0] & 0x0F), (data[1] & 0xFF), (data[2] & 0xFF)));
            } else if ((data[0] & 0xF0) == ShortMessage.CONTROL_CHANGE) {
                listener.controllerChange(new ControlChange((data[0] & 0x0F), (data[1] & 0xFF), (data[2] & 0xFF)));
            }
        }
    }

    /**
     * Adds a listener who will be notified each time a new MIDI message is received from a MIDI input device. If the
     * listener has already been added, it will not be added again.
     *
     * @param listener the listener to add.
     * @return true if and only the listener was sucessfully added.
     * @see #removeMidiListener(MidiListener listener)
     */
    @Override
    public boolean addMidiListener(MidiListener listener) {
        for (MidiListener current : listeners) if (current == listener) return false;

        listeners.add(listener);

        return true;
    }

    /**
     * Removes a given listener.
     *
     * @param listener the listener to remove.
     * @return true if and only the listener was sucessfully removed.
     * @see #addMidiListener(MidiListener listener)
     */
    @Override
    public boolean removeMidiListener(MidiListener listener) {
        for (MidiListener current : listeners) {
            if (current == listener) {
                listeners.remove(listener);
                return true;
            }
        }
        return false;
    }


	/* -- Utilites -- */

    /**
     * It's just convient ... move along...
     */
    private int constrain(int value, int min, int max) {
        if (value > max) value = max;
        if (value < min) value = min;
        return value;
    }

    /**
     * Returns the name of this MidiBus.
     *
     * @return the name of this MidiBus.
     * @see #generateBusName()
     */
    @Override
    public String getBusName() {
        return busName;
    }

    /**
     * Generate a name for this MidiBus instance.
     *
     * @see #getBusName()
     */
    private void generateBusName() {
        String id = new Formatter().format("%08d", System.currentTimeMillis() % 100000000).toString();
        busName = "MidiBus_" + id;
    }

	/* -- Object -- */

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString() {
        String output = "MidiBus: " + busName + " [";
        output += inputDevicesContainers.size() + " input(s), ";
        output += outputDeviceContainers.size() + " output(s), ";
        output += listeners.size() + " listener(s)]";
        return output;
    }

    /**
     * Returns a hash code value for the object.
     *
     * @return a hash code value for this object.
     */
    public int hashCode() {
        return busName.hashCode() + inputDevicesContainers.hashCode() + outputDeviceContainers.hashCode() + listeners.hashCode();
    }

    /**
     * Closes this MidiBus and all connections it has with other MIDI devices.
     * This method exists as per standard javax.sound.midi syntax.
     */
    @Override
    public void close() {
        closeAllMidiDevices();
    }

	/* -- Static methods -- */

    /**
     * Rescan for Midi Devices. This is autocalled once when the MidiBus starts up. It should be called again if you
     * need to refresh the list of available MidiDevices while your program is running.
     */
    public static void findMidiDevices() {
        availableDevices = MidiSystem.getMidiDeviceInfo();
    }

    /**
     * Returns the names of all the available input devices.
     *
     * @return the names of the available inputs.
     * @see #list()
     * @see #availableOutputs()
     * @see #unavailableDevices()
     */
    public static String[] availableInputs() {
        MidiDevice.Info[] deviceInfos = availableInputsMidiDeviceInfo();
        String[] devices = new String[deviceInfos.length];

        for (int i = 0; i < deviceInfos.length; i++) {
            devices[i] = deviceInfos[i].getName();
        }

        return devices;
    }

    /**
     * Returns the names of all the available output devices.
     *
     * @return the names of the available outputs.
     * @see #list()
     * @see #availableInputs()
     * @see #unavailableDevices()
     */
    static public String[] availableOutputs() {
        MidiDevice.Info[] deviceInfos = availableOutputsMidiDeviceInfo();
        String[] devices = new String[deviceInfos.length];

        for (int i = 0; i < deviceInfos.length; i++) {
            devices[i] = deviceInfos[i].getName();
        }

        return devices;
    }

    /**
     * Returns the names of all the unavailable devices.
     *
     * @return the names of the unavailable devices.
     * @see #list()
     * @see #availableInputs()
     * @see #availableOutputs()
     */
    static public String[] unavailableDevices() {
        MidiDevice.Info[] deviceInfos = unavailableMidiDeviceInfo();
        String[] devices = new String[deviceInfos.length];

        for (int i = 0; i < deviceInfos.length; i++) {
            devices[i] = deviceInfos[i].getName();
        }

        return devices;
    }

    /**
     * Returns the MidiDevice.Info of all the available input devices.
     *
     * @return the MidiDevice.Info of the available inputs.
     */
    public static MidiDevice.Info[] availableInputsMidiDeviceInfo() {
        if (availableDevices == null) findMidiDevices();
        MidiDevice device;

        ArrayList<MidiDevice.Info> deviceInfos = new ArrayList<MidiDevice.Info>();

        for (MidiDevice.Info info : availableDevices) {
            try {
                device = MidiSystem.getMidiDevice(info);
                //This open close checks to make sure the announced device is truely available
                //There are many reports on Windows that some devices lie about their availability
                //(For instance the Microsoft GS Wavetable Synth)
                //But in theory I guess this could happen on any OS, so I'll just do it all the time.
                // if(!device.isOpen()) {
                // 	device.open();
                // 	device.close();
                // }
                if (device.getMaxTransmitters() != 0) deviceInfos.add(info);
            } catch (MidiUnavailableException e) {
                //Device was unavailable which is fine, we only care about available inputs
            }
        }

        MidiDevice.Info[] devices = new MidiDevice.Info[deviceInfos.size()];

        deviceInfos.toArray(devices);

        return devices;
    }

    /**
     * Returns the MidiDevice.Info of all the available output devices.
     *
     * @return the MidiDevice.Info of the available output.
     */
    static MidiDevice.Info[] availableOutputsMidiDeviceInfo() {
        if (availableDevices == null) findMidiDevices();
        MidiDevice device;

        ArrayList<MidiDevice.Info> devices_list = new ArrayList();

        for (MidiDevice.Info availableDevice : availableDevices) {
            try {
                device = MidiSystem.getMidiDevice(availableDevice);
                //This open close checks to make sure the announced device is truly available
                //There are many reports on Windows that some devices lie about their availability
                //(For instance the Microsoft GS Wavetable Synth)
                //But in theory I guess this could happen on any OS, so I'll just do it all the time.
                // if(!device.isOpen()) {
                // 	device.open();
                // 	device.close();
                // }
                if (device.getMaxReceivers() != 0) devices_list.add(availableDevice);
            } catch (MidiUnavailableException e) {
                //Device was unavailable which is fine, we only care about available output
            }
        }

        MidiDevice.Info[] devices = new MidiDevice.Info[devices_list.size()];

        devices_list.toArray(devices);

        return devices;
    }

    /**
     * Returns the MidiDevice.Info of all the unavailable devices.
     *
     * @return the MidiDevice.Info of the unavailable devices.
     */
    static MidiDevice.Info[] unavailableMidiDeviceInfo() {
        if (availableDevices == null) findMidiDevices();
        MidiDevice device;

        ArrayList<MidiDevice.Info> unavailbleDeviceInfos = new ArrayList();

        for (int i = 0; i < availableDevices.length; i++) {
            try {
                device = MidiSystem.getMidiDevice(availableDevices[i]);
                //This open close checks to make sure the announced device is truely available
                //There are many reports on Windows that some devices lie about their availability
                //(For instance the Microsoft GS Wavetable Synth)
                //But in theory I guess this could happen on any OS, so I'll just do it all the time.
                // if(!device.isOpen()) {
                // 	device.open();
                // 	device.close();
                // }
            } catch (MidiUnavailableException e) {
                unavailbleDeviceInfos.add(availableDevices[i]);
            }
        }

        MidiDevice.Info[] devices = new MidiDevice.Info[unavailbleDeviceInfos.size()];

        unavailbleDeviceInfos.toArray(devices);

        return devices;
    }

	/* -- Nested Classes -- */

    private class MReceiver implements Receiver {

        MReceiver() {

        }

        public void close() {

        }

        public void send(MidiMessage message, long timeStamp) {

            if (message.getStatus() == ShortMessage.NOTE_ON && message.getMessage()[2] == 0) {
                try {
                    ShortMessage tmp_message = (ShortMessage) message;
                    tmp_message.setMessage(ShortMessage.NOTE_OFF, tmp_message.getData1(), tmp_message.getData2());
                    message = tmp_message;
                } catch (Exception e) {
                    System.err.println("\nThe MidiBus Warning: Mystery error during noteOn (0 velocity) to noteOff conversion");
                }
            }

            notifyListeners(message, timeStamp);
        }

    }

    private class InputDeviceContainer {

        MidiDevice.Info info;

        Transmitter transmitter;
        Receiver receiver;

        InputDeviceContainer(MidiDevice device) {
            this.info = device.getDeviceInfo();
        }

        public boolean equals(Object container) {
            if (container instanceof InputDeviceContainer && ((InputDeviceContainer) container).info.getName().equals(this.info.getName()))
                return true;
            else return false;
        }

        public int hashCode() {
            return info.getName().hashCode();
        }

    }

    private class OutputDeviceContainer {

        MidiDevice.Info info;

        Receiver receiver;

        OutputDeviceContainer(MidiDevice device) {
            this.info = device.getDeviceInfo();
        }

        public boolean equals(Object container) {
            if (container instanceof OutputDeviceContainer &&
                    ((OutputDeviceContainer) container).info.getName().equals(this.info.getName()))
                return true;
            else return false;
        }

        public int hashCode() {
            return info.getName().hashCode();
        }

    }
}
