
public class MoveSizeTest {
  BigInteger boardSize = 0;
  BigInteger pieceCountDefenders = 0;
  BigInteger pieceCountAttackers = 0;
  BigInteger totalPieces = pieceCountDefenders + pieceCountAttackers;
  public BigDecimal positionEstimate = 0;

  public MoveSizeTest(BigInteger size, BigInteger aPieces, BigInteger dPieces) {
    boardSize = size * size
    pieceCountAttackers = aPieces
    pieceCountDefenders = dPieces;
    totalPieces = pieceCountDefenders + pieceCountAttackers;
    factorialTable = new ArrayList<BigInteger>(boardSize + 1);
    for(int i = 0; i < boardSize + 1; i++) {
      factorialTable[i] = 0;
    }
  }
  
  List<BigInteger> factorialTable = new ArrayList<BigInteger>(boardSize + 1);
  
  public BigInteger factorial(BigInteger num) {
    if(factorialTable[num] != 0) {
      return factorialTable[num]
    }
    int origNum = num;
    for(int i = num - 1; i > 0; i--) {
      num *= i
    }
    factorialTable[origNum] = num
    return num
  }
  
  public BigInteger binomial(BigInteger n, BigInteger k) {
    if(k >= n) return 0;
    if(k <= 0) return 1; // One way to choose nothing
    if(n <= 0) return 0;
    else return factorial(n) / (factorial(k) * factorial(n - k))
  }
  
  public BigInteger estimate() {
    println "Estimate for ${Math.sqrt(boardSize)} square with $totalPieces taflmen"
    positionEstimate = 0;
    // Any position with fewer than three pieces is
    // impossible.
    long millis = System.currentTimeMillis();
    for(int i = 3; i < totalPieces + 1; i++) {
      print ".";
      // i is the number of pieces in this set of positions.
      // j is the number of pieces already placed for this set of
      // positions.
      int pieces = i;
      
      // 0.25: tafl has rotational symmetry.
      // factorial(i): for i pieces, there are factorial(i) arrangements of white/black,
      // which represent different positions.
      BigDecimal positionStep = binomial(boardSize, pieces) * 0.25;
      
      BigDecimal sideMultiplier = 0;
      for(int k = 1; k < i - 1; k++) {
	int defendingPieces = 0;
	defendingPieces = k;

	int attackingPieces = i - k;
	BigInteger step = binomial(i, defendingPieces);

	int remainder = i - attackingPieces;

	// The king can be any one of the defending pieces.
	step *= Math.max(1, binomial(defendingPieces, 1));

	// Work out how many special defenders there are/can be above
	// Do the same for attackers
	//step += binomial(defendingPieces - 1, specialDefenders);

	sideMultiplier += step
	//sideMultiplier += binomial(i, k)
      }
      
      positionEstimate += (positionStep * sideMultiplier)
    }
    println "!"
    println "Execution time: ${(System.currentTimeMillis() - millis) / 1000}s"
    return positionEstimate;
  }
}

int boardSize = 7;
println "Naive maximum: " + Math.pow(4, boardSize * boardSize);
println "Less-naive guess: " + ((double)(new MoveSizeTest(boardSize, 5, 8).estimate()));
