package frl.driesprong.bluetooth;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class CommandsTest {

    @Test
    public void testGetReadPumpCommand() throws Exception {

        byte[] output = new byte[]{
                0x01, 0x00, (byte) 0xA7, 0x01, 0x66, 0x54, 0x55, (byte) 0x80,
                0x00, 0x00, 0x02, 0x01, 0x00, (byte) 0x8D, 0x5B, 0x00
        };

        byte[] serial = new byte[]{0x66, 0x54, 0x55};
        byte[] generated = Commands.getReadPumpCommand(serial);

        assertArrayEquals(generated, output);
    }
}