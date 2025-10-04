package chess;

import java.util.*;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        setStartingBoard();
    }

    public ChessBoard(ChessBoard board) {
        squares = new ChessPiece[8][8];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                ChessPiece piece = board.getPiece(new ChessPosition(y + 1, x + 1));
                squares[y][x] = piece;
            }
        }
    }

    public ChessBoard(String boardText) {

    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow() - 1][position.getColumn() - 1] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow() - 1][position.getColumn() - 1];
    }

    public void setBoard(ChessBoard newBoard) {
        squares = newBoard.squares;
    }

    public void setBoardFromText(String boardText) {
        boardText = boardText.strip().replaceAll("(?m)^\\s+", "");
        int row = 8;
        int column = 1;
        for (var c : boardText.toCharArray()) {
            switch (c) {
                case '\n' -> {
                    column = 1;
                    row--;
                }
                case ' ' -> column++;
                case '|' -> {
                }
                default -> {
                    ChessGame.TeamColor color = Character.isLowerCase(c) ? ChessGame.TeamColor.BLACK
                            : ChessGame.TeamColor.WHITE;
                    var type = CHAR_TO_TYPE_MAP.get(Character.toLowerCase(c));
                    var position = new ChessPosition(row, column);
                    var piece = new ChessPiece(color, type);
                    squares[position.getRow() - 1][position.getColumn() - 1] = piece;
                    column++;
                }
            }
        }
    }

    public void setStartingBoard() {
        String startingBoardText = """
                    |r|n|b|q|k|b|n|r|
                    |p|p|p|p|p|p|p|p|
                    | | | | | | | | |
                    | | | | | | | | |
                    | | | | | | | | |
                    | | | | | | | | |
                    |P|P|P|P|P|P|P|P|
                    |R|N|B|Q|K|B|N|R|
                """;
        setBoardFromText(startingBoardText);

    }

    private static final Map<Character, ChessPiece.PieceType> CHAR_TO_TYPE_MAP = Map.of(
            'p', ChessPiece.PieceType.PAWN,
            'n', ChessPiece.PieceType.KNIGHT,
            'r', ChessPiece.PieceType.ROOK,
            'q', ChessPiece.PieceType.QUEEN,
            'k', ChessPiece.PieceType.KING,
            'b', ChessPiece.PieceType.BISHOP);

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */

    public void resetBoard() {
        this.squares = new ChessPiece[8][8];
        this.setStartingBoard();
    }

    public Collection<ChessPosition> getPiecePositions(ChessGame.TeamColor color, ChessPiece.PieceType pieceType) {
        Collection<ChessPosition> positions = new ArrayList<>();
        for (int y = 0; y < squares.length; y++) {
            for (int x = 0; x < squares[y].length; x++) {
                ChessPiece piece = squares[y][x];
                if (piece != null && piece.getTeamColor() == color && piece.getPieceType() == pieceType) {
                    positions.add(new ChessPosition(y + 1, x + 1));
                }
            }
        }
        return positions;
    }

    public Collection<ChessPosition> getTeamPiecePositions(ChessGame.TeamColor color) {
        List<ChessPosition> positions = new ArrayList<>();
        for (int y = 0; y < squares.length; y++) {
            for (int x = 0; x < squares[y].length; x++) {
                ChessPiece piece = squares[y][x];
                if (piece != null && piece.getTeamColor() == color) {
                    positions.add(new ChessPosition(y + 1, x + 1));
                }
            }
        }
        return positions;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(squares, that.squares);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(squares);
    }

    @Override
    public String toString() {
        String printStr = "";
        for (int y = 8; y > 0; y--) {
            printStr += "|";
            for (int x = 1; x < 9; x++) {
                ChessPosition pos = new ChessPosition(y, x);
                ChessPiece pieceAtPos = getPiece(pos);
                if (pieceAtPos == null) {
                    printStr += " ";
                } else {
                    printStr += pieceAtPos.toString();
                }
                printStr += "|";
            }
            printStr += '\n';
        }
        return printStr;
    }
}
