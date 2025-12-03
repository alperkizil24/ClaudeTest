# Prime Number Finder

A Java console application that finds and displays all prime numbers within a specified interval.

## Features

- Accepts two integers as input to define an interval
- Efficiently checks for prime numbers using optimized algorithm
- Displays all prime numbers in the given range
- Shows the total count of prime numbers found
- Handles intervals in any order (automatically swaps if needed)

## How to Compile

```bash
javac PrimeFinder.java
```

## How to Run

```bash
java PrimeFinder
```

## Usage Example

```
==============================================
     PRIME NUMBER FINDER
==============================================

Enter the first integer: 10
Enter the second integer: 50

Prime numbers between 10 and 50:
------------------------------------------------
11 13 17 19 23 29 31 37 41 43
47
------------------------------------------------
Total prime numbers found: 11

Thank you for using Prime Number Finder!
```

## Algorithm

The application uses an optimized prime checking algorithm:
1. Handles edge cases (numbers < 2, even numbers)
2. Only checks odd divisors up to the square root of the number
3. Time complexity: O(√n) for each number checked
