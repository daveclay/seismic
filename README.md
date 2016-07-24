## Seismic

Swing app written in Scala that listens for incoming trigger and phrase messages on a serial bus, interprets the messsages based on configuration, and sends out midi messages on a midi bus. Used by the custom Piston-Rails drum machine.

# Usage

```bash
java -jar seismic.jar "/dev/tty.usbserial-16TNB297" "IAC Bus 2"
```
