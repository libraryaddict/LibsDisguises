package me.libraryaddict.disguise.utilities;

import me.libraryaddict.disguise.disguisetypes.watchers.GridLockedWatcher;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class DisguiseGridLockedTest {
    @ParameterizedTest
    @CsvSource(value = {
        // Basic tests
        "0, 1, 0.5", // Ensure that a basic width of 1 block is centered
        "1, 1, 1.5", // Ensure that a basic width of 1 block is centered
        "2, 1, 2.5", // Ensure that a basic width of 1 block is centered
        "-1, 1, -0.5",  // Ensure it works for negative numbers
        // Get a little more complicated
        "1.3, 1, 1.5", // Ensure it works with decimals
        "1.01, 1, 1.5", // Ensure it works with decimals
        "1.99, 1, 1.5", // Ensure it works with decimals
        "-1.3, 1, -1.5", // Ensure it works with negative numbers
        "-0.9, 1, -0.5",  // Ensure it works with negative numbers
        // Start testing larger widths
        "10, 2, 10", // Test a larger width of 2 blocks
        "-10, 2, -10", // Test it works with negative numbers
        "10, 3, 10.5", // Test with a width of 3
        // Test that we're handling block displays with tiny scales
        "10, 0.25, 10.125", // With a block that's 0.25 wide, Covers 0.00 - 0.25
        "10.3, 0.25, 10.375",// With a block that's 0.25 wide, Covers 0.25 - 0.50
        "10.6, 0.25, 10.625", // With a block that's 0.25 wide, Covers 0.50 - 0.75
        "10.9, 0.25, 10.875", // With a block that's 0.25 wide, Covers 0.75 - 1.00
    })
    public void testGridCentering(double origin, double width, double expected) {
        // The player is standing at cords 0
        double offset = GridLockedWatcher.center(origin, width);

        Assertions.assertEquals(expected, offset, "With an origin of " + origin + " and a width of " + width);
    }
}
