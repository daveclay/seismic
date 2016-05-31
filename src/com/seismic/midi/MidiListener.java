package com.seismic.midi;

import javax.sound.midi.MidiMessage;

public interface MidiListener {

    /**
     * Objects notifying this ObjectMidiListener of a new NoteOn events call this method.
     *
     * @param note the note object associated with this event
     */
    public void noteOn(Note note);

    /**
     * Objects notifying this ObjectMidiListener of a new NoteOff events call this method.
     *
     * @param note the note object associated with this event
     */
    public void noteOff(Note note);

    /**
     * Objects notifying this ObjectMidiListener of a new ControllerChange events call this method.
     *
     * @param change the ControlChange object associated with this event
     */
    public void controllerChange(ControlChange change);

    /**
     * Objects notifying this SimpleMidiListener of a new NoteOn MIDI message call this method.
     *
     * @param channel  the channel on which the NoteOn arrived
     * @param pitch    the pitch associated with the NoteOn
     * @param velocity the velocity associated with the NoteOn
     */
    public void noteOn(int channel, int pitch, int velocity);

    /**
     * Objects notifying this SimpleMidiListener of a new NoteOff MIDI message call this method.
     *
     * @param channel  the channel on which the NoteOff arrived
     * @param pitch    the pitch associated with the NoteOff
     * @param velocity the velocity associated with the NoteOff
     */
    public void noteOff(int channel, int pitch, int velocity);

    /**
     * Objects notifying this SimpleMidiListener of a new ControllerChange MIDI message call this method.
     *
     * @param channel the channel on which the ContollerChange arrived
     * @param number  the controller number associated with the ContollerChange
     * @param value   the controller value associated with the ContollerChange
     */
    public void controllerChange(int channel, int number, int value);

    /**
     * Objects notifying this StandardMidiListener of a new MIDI message call this method and pass the MidiMessage
     *
     * @param message the MidiMessage received
     */
    public void midiMessage(MidiMessage message, long timeStamp);
}
