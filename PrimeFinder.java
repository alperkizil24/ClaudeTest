import java.util.Scanner;

/**
 * Console application to find and display all prime numbers in a given interval
 */
public class PrimeFinder {

    /**
     * Checks if a number is prime
     * @param num The number to check
     * @return true if the number is prime, false otherwise
     */
    public static boolean isPrime(int num) {
        // Numbers less than 2 are not prime
        if (num < 2) {
            return false;
        }

        // 2 is prime
        if (num == 2) {
            return true;
        }

        // Even numbers (except 2) are not prime
        if (num % 2 == 0) {
            return false;
        }

        // Check odd divisors up to square root of num
        for (int i = 3; i * i <= num; i += 2) {
            if (num % i == 0) {
                return false;
            }
        }

        return true;
    }

    /**
     * Finds and displays all prime numbers in the given interval
     * @param start Starting number of the interval (inclusive)
     * @param end Ending number of the interval (inclusive)
     */
    public static void findPrimesInInterval(int start, int end) {
        // Ensure start is not greater than end
        if (start > end) {
            int temp = start;
            start = end;
            end = temp;
        }

        System.out.println("\nPrime numbers between " + start + " and " + end + ":");
        System.out.println("------------------------------------------------");

        int count = 0;
        for (int i = start; i <= end; i++) {
            if (isPrime(i)) {
                System.out.print(i + " ");
                count++;

                // Print newline every 10 numbers for better readability
                if (count % 10 == 0) {
                    System.out.println();
                }
            }
        }

        System.out.println("\n------------------------------------------------");
        System.out.println("Total prime numbers found: " + count);
    }

    /**
     * Main method - entry point of the application
     */
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("==============================================");
        System.out.println("     PRIME NUMBER FINDER");
        System.out.println("==============================================");
        System.out.println();

        try {
            // Get the first integer
            System.out.print("Enter the first integer: ");
            int num1 = scanner.nextInt();

            // Get the second integer
            System.out.print("Enter the second integer: ");
            int num2 = scanner.nextInt();

            // Find and display prime numbers in the interval
            findPrimesInInterval(num1, num2);

        } catch (Exception e) {
            System.out.println("\nError: Invalid input! Please enter valid integers.");
        } finally {
            scanner.close();
        }

        System.out.println("\nThank you for using Prime Number Finder!");
    }
}
