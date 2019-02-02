import java.util.LinkedHashMap;
import java.util.Map.Entry;
import java.util.*;
import java.io.*;

class homework {

	static final int EMPTY = -6;
	static final int SQUARED = 2;
	static int MAX_DEPTH = 5;
	static int currentDepthLimit;
	static  int MAX_VALUE = Integer.MAX_VALUE;
	static  int MIN_VALUE = Integer.MIN_VALUE;
	static float timeLeft;
	static long start;
	static long estimatedTime;
	static boolean endSearch;
	static ArrayList<Integer> gameManager;

	public static ArrayList<List> exploreTree(fruitBoard node) {

		ArrayList<List> moveList = new ArrayList<List>();
		ArrayList<ArrayList<List>> allMoves = node.getOptions();
		int[][] initialBoard = node.getState();
		int[][] newBoard = new int[initialBoard.length][initialBoard.length];
		int length = initialBoard.length;
		int utility;
		int current;
		int globalBest = MIN_VALUE;
		int branchingFactor = node.getOptions().size();

		fruitBoard child;
		boolean max = true;
		int index = 0;
		// check branching factor and adjust max depth
		if (branchingFactor >= 100) {
			MAX_DEPTH-=3;
			System.out.println("Our max search depth has lowered to: " + MAX_DEPTH + " because of a branching factor >= 100");
		} else if (branchingFactor >= 70 && branchingFactor < 100 ) {
			MAX_DEPTH-=2;
			while (MAX_DEPTH <= 0) MAX_DEPTH++;
			System.out.println("Our max search depth has lowered to: " + MAX_DEPTH + " because of a branching factor >= 70 & < 100");
		}
		if (timeLeft <= 5000) {
			MAX_DEPTH-=3;
			while (MAX_DEPTH <= 0) MAX_DEPTH++;
			System.out.println("Our max search depth has lowered to: " + MAX_DEPTH + " because of time remaining");
		} else if (timeLeft <= 10000) {
			MAX_DEPTH-=2;
			while (MAX_DEPTH <= 0) MAX_DEPTH++;
			System.out.println("Our max search depth has lowered to: " + MAX_DEPTH + " because of time remaining");
		}
		if (timeLeft <= 5000 && branchingFactor <= 70) {
			MAX_DEPTH--;
			while (MAX_DEPTH <= 0) MAX_DEPTH++;
			System.out.println("Our max search depth has lowered to: " + MAX_DEPTH + " because of special conditions");

		}

		for (ArrayList<List> move : allMoves) {

			// generate	 new board
			newBoard = new int[length][length];
			for (int j = 0; j < length; ++j) {
				System.arraycopy(initialBoard[j], 0, newBoard[j], 0, length);
			}

			utility = (int) Math.pow(move.size(), SQUARED);

			child = new fruitBoard(new ArrayList<ArrayList<List>>(), newBoard);
			child.updateState(move);
			child.declareComponents();

			current = iterativeDeepening(child, utility, index);

			if (current > globalBest) {
				globalBest = current;
				moveList = move;
			}

			//out of time, break out early
			if (endSearch) {
				return moveList;
			}
			++index;
		}

		return moveList;
	}

	public static int iterativeDeepening(fruitBoard root, int utility, int index) {

		int valueOfState = MIN_VALUE;
		int deepestUtility = MIN_VALUE;
		int currentValue = -1;
		int alpha = MIN_VALUE;
		int beta = MAX_VALUE;
		int startingDepth = 1;
		currentDepthLimit = 1;
		int reference = MIN_VALUE;
		boolean max = false;
		endSearch = false;

		while (currentDepthLimit <= MAX_DEPTH) {

			currentValue = alphaBetaPruning(root, max, utility, startingDepth, alpha, beta);

			// We have reached a dead board, break out of IDS
			if (reference == currentValue) {
				break;
			}

			// if alpha beta thread runs out of time, abandon its results
			if (!endSearch) {
				// stores alpha beta return value for each depth of this child
				deepestUtility = currentValue;

				if (gameManager.size() == index ) {
					gameManager.add(deepestUtility);
				}
	
			} else {
				if (gameManager.size() == index) {
						deepestUtility = currentValue;
						gameManager.add(deepestUtility);
						return deepestUtility;
				} else {
					int temp = gameManager.get(index);
					if ( temp > currentValue) {
						return gameManager.get(index);
					}
				}
			}

			// Set reference to "previous" depth value
			reference = deepestUtility;

			++currentDepthLimit;
		}	
		return deepestUtility;
	}

	public static int alphaBetaPruning(fruitBoard node, boolean maxPlayer, int utility, int depth, int alpha, int beta) {

		int[][] board;
		int[][] newState;
		int size;
		int length;
		int v;
		int score;
		int newUtility;
		ArrayList<ArrayList<List>> allMoves;
		fruitBoard child;
		board = node.getState();
		length = board.length;
		long beganAt = System.currentTimeMillis();

		// 0.01 seconds away from game over, return value;
		if (beganAt + 0.01 >= (estimatedTime)) {
			endSearch = true;
		}

		if ( endSearch || node.terminalBoard() || depth == currentDepthLimit ) {
			return utility;

		} else {

			allMoves = node.getOptions();
			size = allMoves.size();
			int i;

			if (maxPlayer) {
	
				v = MIN_VALUE;

				for (i = 0; i < size; ++i) {

					// generate	 new board
					newState = new int[length][length];
					for (int j = 0; j < length; ++j) {
						System.arraycopy(board[j], 0, newState[j], 0, length);
					}

					newUtility = (int) Math.pow(allMoves.get(i).size(), SQUARED);

					child = new fruitBoard(new ArrayList<ArrayList<List>>(), newState);
					child.updateState(allMoves.get(i));
					child.declareComponents();

					score = utility + newUtility;

					v = Math.max( v, alphaBetaPruning(child, false, score, depth+1, alpha, beta) );
					alpha = Math.max( alpha, v );

					// cut off beta
					if (beta <= alpha) break;
				}
				return v;

			} else {	

				v = MAX_VALUE;

				for (i = 0; i < size; ++i) {

					// generate	 new board
					newState = new int[length][length];
					for (int j = 0; j < length; ++j) {
						System.arraycopy(board[j], 0, newState[j], 0, length);
					}

					newUtility = (int) Math.pow(allMoves.get(i).size(), SQUARED);

					child = new fruitBoard(new ArrayList<ArrayList<List>>(), newState);
					child.updateState(allMoves.get(i));
					child.declareComponents();

					score = utility - newUtility;

					v = Math.min( v, alphaBetaPruning(child, true, score, depth+1, alpha, beta) );
					beta = Math.min( beta, v );

					// cut off alpha
					if (beta <= alpha) break;
				}
				return v;
			}
		}
	}

	public static String printBoard(fruitBoard node, ArrayList<List> move) {

		int[][] board;
		String fruit;
		StringBuilder sb;

		board = node.getState();
		sb = new StringBuilder();

		if (move.size() >= 1) {
			sb.append( (char)('A' + (int)move.get(0).get(1)) + "" + ((int)move.get(0).get(0)+1));
			sb.append("\n");
		}	

		for (int i = 0; i < board.length; ++i) {
			for (int j = 0; j < board.length; ++j) {
				if (board[i][j] == EMPTY ) {
					sb.append("*");
				} else {
					fruit = Integer.toString(board[i][j]);
					sb.append(fruit);
				}
				if (i != board.length-1 && j == board.length-1) {
					sb.append("\n");
				} 
			}
		}
		return sb.toString();
	}

	public static void main(String[] arg) {

		start = System.currentTimeMillis();
		ArrayList<String> fileContents = new ArrayList<String>();
		ArrayList<List> choice;
		int[][] solutionSpace;
		int branchingFactor;
		fruitBoard node;

		try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {

			String currentLine;

			while ((currentLine = br.readLine()) != null) {
				fileContents.add(currentLine); 
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		//initialize basic problem parameters and data structures
		int dimensions = Integer.parseInt(fileContents.get(0));
		int numFruits = Integer.parseInt(fileContents.get(1));
		timeLeft = Float.parseFloat(fileContents.get(2));
		timeLeft*=1000;

		estimatedTime = (long)timeLeft + start;

		solutionSpace = new int[dimensions][dimensions];
		gameManager = new ArrayList<Integer>();

		//initialize  board
		char[] chArr = new char[dimensions];
		for (int row = 0; row < dimensions; ++row) {
			chArr = fileContents.get(row+3).toCharArray();
			for (int col = 0; col < dimensions; ++col) {
				solutionSpace[row][col] = chArr[col] - '0'; 
			}
		}

		node = new fruitBoard(new ArrayList<ArrayList<List>>(), solutionSpace);
		node.declareComponents();

		choice = exploreTree(node);
		node.updateState(choice);

		long end = System.currentTimeMillis();
		long time = end - start;
		System.out.println("total time taken to complete search: " + time + " miliseconds");

		try {
		    PrintWriter writer = new PrintWriter("output.txt", "UTF-8");
			writer.println(printBoard(node, choice));
			writer.close();
		} catch (IOException e) {
		   System.out.println("Error");
		}
	}
}

class fruitBoard {

	public static final int EMPTY = -6;

	protected int fruitType;
	protected int value;
	protected int[][] board;
	protected ArrayList<ArrayList<List>> options;

	public fruitBoard(ArrayList<ArrayList<List>> options, int[][] board) {
		this.options = options;
		this.board = board;
		this.value = 0;
	}

	public void declareComponents() {

		int currentFruitType;
		int length;
		int counter;
		int[][] board;
		Set<List> total;
		ArrayList<ArrayList<List>> allComponents;
		ArrayList<List> coordPairList;
		List<Integer> coordPair;

		counter = 0;
		total = new HashSet<List>();
		allComponents = new ArrayList<ArrayList<List>>();
		board = this.board;
		length = board.length;

		for (int row = 0; row < length; ++row) {
			for (int col = 0; col < length; ++col) {

				if (board[row][col] != EMPTY) {

					coordPairList = new ArrayList<List>();
					coordPair = new ArrayList<Integer>();

					coordPair.add(row);
					coordPair.add(col);

					currentFruitType = board[row][col];

					if (!total.contains(coordPair)) {
						// run DFS, only expanding the same fruit type
						DFS(board, coordPairList, currentFruitType, coordPair);
						// adds group of coordinates comprising a block 
						allComponents.add(coordPairList);
						// ensures no repeat values added
						total.addAll(coordPairList);
					}
				}
			}
		}
		// set nodes options property to coords of all potential islands
		Collections.sort(allComponents, new Comparator<ArrayList>() {
			public int compare(ArrayList a1, ArrayList a2) {
				return a2.size() - a1.size();
			}
		});
		this.options = allComponents;
	}

	public static void DFS(int[][] board, ArrayList<List> visited, int fruitType, List<Integer> pair) {

		visited.add(pair);
		
		ArrayList<List> neighbors = findNeighbors(board, visited, fruitType, pair);
		ArrayList<Integer> temp;

		int size = neighbors.size();

		// if valid neighbors, call function on neighbor coordinate
		if (size > 0) {
			for (int i = 0; i < size; ++i) {

				if (!visited.contains(neighbors.get(i))) {

					temp = new ArrayList<Integer>(neighbors.get(i));
					DFS(board, visited, fruitType, temp);
				}
			}
		}
	} 

	public static ArrayList<List> findNeighbors(int[][] board, ArrayList<List> visited, int currentFruitType, List<Integer> pair) {

		ArrayList<Integer> coordPair = new ArrayList<Integer>();
		ArrayList<List> neighbors = new ArrayList<List>();

		int row = pair.get(0);
		int col = pair.get(1);

		//check up
		coordPair.add(row-1);
		coordPair.add(col);
		if (row != 0 && board[row-1][col] == currentFruitType && !visited.contains(coordPair)) {
			neighbors.add(coordPair);
		}

		//check down
		coordPair = new ArrayList<Integer>();
		coordPair.add(row+1);
		coordPair.add(col);
		if (row != board.length-1 && board[row+1][col] == currentFruitType && !visited.contains(coordPair)) {
			neighbors.add(coordPair);
		}

		//check left
		coordPair = new ArrayList<Integer>();
		coordPair.add(row);
		coordPair.add(col-1);
		if (col != 0 && board[row][col-1] == currentFruitType && !visited.contains(coordPair)) {
			neighbors.add(coordPair);
		}

		// check right
		coordPair = new ArrayList<Integer>();
		coordPair.add(row);
		coordPair.add(col+1);
		if (col != board.length-1 && board[row][col+1] == currentFruitType && !visited.contains(coordPair)) {
			neighbors.add(coordPair);
		}
		return neighbors;
	}

	public void updateState(ArrayList<List> currentDecision) {

		int numberElementsBelow;
		int numColVisited;
		int currentRow;
		int currentCol;
		int temp;
		int boardLength;
		int size;
		int row;
		int col;
		int[][] board;
		boolean columnFormatted;
		List coordPair;
		ArrayList<Integer> columns;

		board = this.getState();
		coordPair = new ArrayList();
		size = currentDecision.size();
		for (int i = 0; i < size; ++i) {

			coordPair = currentDecision.get(i);
			row = (Integer) coordPair.get(0);
			col = (Integer) coordPair.get(1);

			board[row][col] = EMPTY;
		}

		// generate list of columns we'll need to change
		columns = new ArrayList<Integer>();
		for (int j = 0; j < size; ++j) {
			coordPair = currentDecision.get(j);
			col = (Integer) coordPair.get(1);
			if (columns.indexOf(col) == -1) {
				columns.add(col);
			}
		}

		//apply gravity to affected columns
		numColVisited = 0;
		boardLength = board.length;
		while (numColVisited < columns.size()) {

			currentCol = columns.get(numColVisited); 

			for (int j = boardLength - 1; j >= 0; --j) {

				numberElementsBelow = 0;

				currentRow = j;

				// boolean determines whether current fruit properly placed
				columnFormatted = false;
				// determine number of empty spots above
				while (currentRow > 0) {
					// if fruit properly placed, exit
					if (columnFormatted) {
						break;
					} else {
						// examine current index, if empty move up
	 					if (board[currentRow][currentCol] == EMPTY) {

	 						++numberElementsBelow;
	 						--currentRow;

	 					} else if (board[currentRow][currentCol] != EMPTY && numberElementsBelow > 0) {
	 						// we've encountered a fruit. Swap with numBelow
	 						temp = board[currentRow][currentCol];
	 						board[currentRow][currentCol] = EMPTY;
	 						board[currentRow+numberElementsBelow][currentCol] = temp;
	 						columnFormatted = true;

	 					} else {
	 						// if first element is a fruit, decrement row only
	 						--currentRow;
	 					}
	 					// if row 0 reached and a fruit
	 					if (!columnFormatted && currentRow == 0 && board[currentRow][currentCol] != EMPTY && numberElementsBelow > 0) {
	 						// immediately check if element needs to move down
	 						temp = board[currentRow][currentCol];
	 						board[currentRow][currentCol] = EMPTY;
	 						board[currentRow+numberElementsBelow][currentCol] = temp;

	 					} else if (!columnFormatted && currentRow == 0 && board[currentRow][currentCol] == EMPTY) {
	 						columnFormatted = true;
	 					}
 					} 
				} 
			}
			++numColVisited;
		}
	}

	public  boolean terminalBoard() {

		int[][] state;
		int boardLength;

		state = this.getState();
		boardLength = board.length - 1;

		for (int row = boardLength; row >= 0; --row) {
			for (int col = boardLength; col >= 0; --col) {
				if (state[row][col] != EMPTY) {
					return false;
				}
			}
		}
		return true;
	}

	public int getValue() {
		return this.value;
	}

	public int[][] getState() {
		return this.board;
	}

	public ArrayList<ArrayList<List>> getOptions(){
		return this.options;
	}
}
