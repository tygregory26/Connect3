import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

/**
 * @author tygregory
 */
public class ConnectThreeGUI extends JFrame {

	private final LinkedList<Player> players;
	private String[][] gameBoard;
	private String currentPlayer;
	private JButton[] columnButtons;
	private Map<String, Color> playerColors;
	private Stack<String[][]> moveHistory;

	private JTextArea instructionsTextArea;

	public ConnectThreeGUI() {
		players = new LinkedList<>();
		currentPlayer = "";
		gameBoard = new String[6][7];
		columnButtons = new JButton[7];
		playerColors = new HashMap<>();
		moveHistory = new Stack<>();

		displayWelcomeMessage();

		initializePlayers();
		initializeGameBoard();
		setupUI();
	}

	private void displayWelcomeMessage() {
		JOptionPane.showMessageDialog(this, "Welcome to Connect Three!\nClick 'OK' to enter player names.", "Welcome",
				JOptionPane.INFORMATION_MESSAGE);
	}

	private void initializePlayers() {
		String player1Name = JOptionPane.showInputDialog("Enter Player 1's name:");
		String player2Name = JOptionPane.showInputDialog("Enter Player 2's name:");

		if (player1Name == null || player1Name.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Player 1's name cannot be empty. Please enter a name.");
			initializePlayers();
			return;
		}

		if (player2Name == null || player2Name.isEmpty()) {
			JOptionPane.showMessageDialog(this, "Player 2's name cannot be empty. Please enter a name.");
			initializePlayers();
			return;
		}

		players.add(new Player(player1Name));
		players.add(new Player(player2Name));

		playerColors.put(players.getFirst().getName(), Color.RED);
		playerColors.put(players.getLast().getName(), Color.BLUE);

		currentPlayer = players.getFirst().getName();
	}

	private void initializeGameBoard() {
		for (int row = 0; row < gameBoard.length; row++) {
			for (int col = 0; col < gameBoard[row].length; col++) {
				gameBoard[row][col] = "";
			}
		}
	}

	private void setupUI() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setTitle("Connect Three Game");

		JPanel buttonPanel = new JPanel(new GridLayout(1, 7));
		for (int i = 0; i < columnButtons.length; i++) {
			columnButtons[i] = new JButton("Column " + (i + 1));
			columnButtons[i].addActionListener(new ColumnButtonListener(i));
			buttonPanel.add(columnButtons[i]);
		}

		JButton switchPlayerButton = new JButton("Switch Player");
		switchPlayerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				switchPlayer();
			}
		});

		JButton undoButton = new JButton("Undo");
		undoButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				undoMove();
			}
		});

		instructionsTextArea = new JTextArea();
		instructionsTextArea.setEditable(false);
		instructionsTextArea.setLineWrap(true);
		instructionsTextArea.setWrapStyleWord(true);
		instructionsTextArea.setText("Instructions:\n" + "1. Click on a column button to drop your chip.\n"
				+ "2. Connect three chips in a row (horizontally, vertically, or diagonally) to win.\n"
				+ "3. You can undo your last move using the 'Undo' button.");

		JScrollPane instructionsScrollPane = new JScrollPane(instructionsTextArea);
		instructionsScrollPane.setPreferredSize(new Dimension(200, 300));

		JPanel controlPanel = new JPanel();
		controlPanel.add(buttonPanel);
		controlPanel.add(switchPlayerButton);
		controlPanel.add(undoButton);

		BoardPanel boardPanel = new BoardPanel();

		setLayout(new BorderLayout());
		add(controlPanel, BorderLayout.NORTH);
		add(boardPanel, BorderLayout.CENTER);
		add(instructionsScrollPane, BorderLayout.EAST);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);

		updateTurnText();
	}

	private void dropChip(int column) {
		String[][] previousState = cloneGameBoard(gameBoard);
		moveHistory.push(previousState);

		int row = findAvailableRow(column);

		if (row != -1) {
			gameBoard[row][column] = currentPlayer;

			if (checkForWin(row, column)) {
				int option = JOptionPane.showOptionDialog(this, currentPlayer + " wins! Do you want to play again?",
						"Game Over", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

				if (option == JOptionPane.YES_OPTION) {
					resetGame();
				} else {
					System.exit(0);
				}
			} else {
				switchPlayer();
				repaint();
			}
		} else {
			JOptionPane.showMessageDialog(this, "Column is full! Choose another column.");
		}
	}

	private int findAvailableRow(int column) {
		for (int row = gameBoard.length - 1; row >= 0; row--) {
			if (gameBoard[row][column].isEmpty()) {
				return row;
			}
		}
		return -1;
	}

	private void switchPlayer() {
		if (!players.isEmpty()) {
			players.addLast(players.removeFirst());
			currentPlayer = players.getFirst().getName();
			updateTurnText();
		}
	}

	private void undoMove() {
		if (!moveHistory.isEmpty()) {
			gameBoard = moveHistory.pop();
			switchPlayer();
			updateTurnText();
			repaint();
		} else {
			JOptionPane.showMessageDialog(this, "There's nothing to undo.", "Undo Move",
					JOptionPane.INFORMATION_MESSAGE);
		}
	}

	private void resetGame() {
		int option = JOptionPane.showOptionDialog(this, "Do you want to play again?", "Game Over",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, null, null);

		if (option == JOptionPane.YES_OPTION) {
			players.clear();
			initializePlayers();
			initializeGameBoard();
			moveHistory.clear();
			updateTurnText();
			repaint();
		} else {
			System.exit(0);
		}
	}

	private void updateTurnText() {
		setTitle("Connect Three Game - " + currentPlayer + "'s turn");
	}

	private String[][] cloneGameBoard(String[][] original) {
		String[][] clone = new String[original.length][original[0].length];
		for (int i = 0; i < original.length; i++) {
			System.arraycopy(original[i], 0, clone[i], 0, original[i].length);
		}
		return clone;
	}

	private boolean checkForWin(int row, int col) {
		String chip = gameBoard[row][col];

		for (int c = 0; c <= gameBoard[row].length - 3; c++) {
			if (gameBoard[row][c].equals(chip) && gameBoard[row][c + 1].equals(chip)
					&& gameBoard[row][c + 2].equals(chip)) {
				return true;
			}
		}

		for (int r = 0; r <= gameBoard.length - 3; r++) {
			if (gameBoard[r][col].equals(chip) && gameBoard[r + 1][col].equals(chip)
					&& gameBoard[r + 2][col].equals(chip)) {
				return true;
			}
		}

		for (int r = 0; r <= gameBoard.length - 3; r++) {
			for (int c = 0; c <= gameBoard[row].length - 3; c++) {
				if (gameBoard[r][c].equals(chip) && gameBoard[r + 1][c + 1].equals(chip)
						&& gameBoard[r + 2][c + 2].equals(chip)) {
					return true;
				}
			}
		}

		for (int r = 2; r < gameBoard.length; r++) {
			for (int c = 0; c <= gameBoard[row].length - 3; c++) {
				if (gameBoard[r][c].equals(chip) && gameBoard[r - 1][c + 1].equals(chip)
						&& gameBoard[r - 2][c + 2].equals(chip)) {
					return true;
				}
			}
		}

		return false;
	}

	private class BoardPanel extends JPanel {

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);

			int cellSize = 80;

			for (int row = 0; row < gameBoard.length; row++) {
				for (int col = 0; col < gameBoard[row].length; col++) {
					g.drawRect(col * cellSize, row * cellSize, cellSize, cellSize);
					if (!gameBoard[row][col].isEmpty()) {
						g.setColor(playerColors.get(gameBoard[row][col]));
						g.fillOval(col * cellSize, row * cellSize, cellSize, cellSize);
					}
				}
			}
		}
	}

	private class ColumnButtonListener implements ActionListener {
		private final int columnIndex;

		public ColumnButtonListener(int columnIndex) {
			this.columnIndex = columnIndex;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			dropChip(columnIndex);
		}
	}

	private static class Player {
		private final String name;

		public Player(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new ConnectThreeGUI());
	}
}
