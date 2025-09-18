package chess;

import java.util.*;
import java.util.stream.Stream;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * @return list of potential promotion moves for pawn
     */
    public List<ChessMove> getPromotionMoves(ChessPosition position1, ChessPosition position2) {
        List<ChessMove> moves = new ArrayList<>();
        List<ChessPiece.PieceType> types = Arrays.asList(PieceType.QUEEN, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK);
        for (ChessPiece.PieceType type : types) {
            moves.add(new ChessMove(position1, position2, type));
        }
        return moves;
    }

    /**
     * @return list of potential moves for a piece that can move one time per turn (king, knight)
     */
    public List<ChessMove> getMoves(List<ChessMove> validMoves, int[][] directionsArr, ChessBoard board, ChessPosition position, ChessPiece piece) {
        for (int[] dir : directionsArr) {
            int dx = dir[0], dy = dir[1];
            int x = position.getColumn() + dx;
            int y = position.getRow() + dy;
            boolean inBounds = (x > 0 && y > 0 && x < 9 && y < 9);
            if (inBounds) {
                ChessPosition nextPos = new ChessPosition(y, x);
                ChessPiece pieceAtPos = board.getPiece(nextPos);
                if (pieceAtPos == null || (pieceAtPos.getTeamColor() != piece.getTeamColor())) {
                    validMoves.add(new ChessMove(position, nextPos, null));
                }
            }
        }
        return validMoves;
    }

    /**
     * @return list of potential moves for a pawn
     */
    public List<ChessMove> getPawnMoves(List<ChessMove> validMoves, ChessBoard board, ChessPosition position, ChessPiece piece) {
        ChessGame.TeamColor myPieceColor = piece.getTeamColor();
        int dy = (myPieceColor == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow  = (myPieceColor == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int[] xOptions = {-1, 0, 1};

        for (int dx : xOptions) {
            int x = position.getColumn() + dx;
            int y = position.getRow() + dy;
            boolean inBounds = (x > 0 && y > 0 && x < 9 && y < 9);
            boolean promotePawn = (
                    (myPieceColor == ChessGame.TeamColor.WHITE && y == 8) ||
                            (myPieceColor == ChessGame.TeamColor.BLACK && y == 1)
            );
            if (inBounds) {
                ChessPosition nextPos = new ChessPosition(y, x);
                ChessPiece pieceAtPos = board.getPiece(nextPos);
                if (dx == 0) {
                    if (pieceAtPos == null) {
                        if (promotePawn)  {
                            validMoves.addAll(getPromotionMoves(position, nextPos));
                        } else {
                            validMoves.add(new ChessMove(position, nextPos, null));
                            // Include moving 2 squares on first move
                            if (position.getRow() == startRow) {
                                y = y + dy;
                                nextPos = new ChessPosition(y, x);
                                pieceAtPos = board.getPiece(nextPos);
                                if (pieceAtPos == null) {
                                    validMoves.add(new ChessMove(position, nextPos, null));
                                }
                            }
                        }
                    }
                } else {
                    if (pieceAtPos != null && pieceAtPos.getTeamColor() != myPieceColor) {
                        if (promotePawn)  {
                            validMoves.addAll(getPromotionMoves(position, nextPos));
                        } else {
                            validMoves.add(new ChessMove(position, nextPos, null));
                        }
                    }
                }
            }
        }
        return validMoves;
    }

    /**
     * @return list of potential moves for a piece that can move multiple times per turn (queen, rook, bishop)
     */
    public List<ChessMove> loopGetMoves(List<ChessMove> validMoves, int[][] directionsArr, ChessBoard board, ChessPosition position, ChessPiece piece) {
        for (int[] dir : directionsArr) {
            int dx = dir[0], dy = dir[1];
            int x = position.getColumn() + dx;
            int y = position.getRow() + dy;

            while (x > 0 && y > 0 && x < 9 && y < 9) {
                ChessPosition nextPos = new ChessPosition(y, x);
                ChessPiece pieceAtPos = board.getPiece(nextPos);

                if (pieceAtPos != null) {
                    if (pieceAtPos.getTeamColor() != piece.getTeamColor()) {
                        validMoves.add(new ChessMove(position, nextPos, null));
                    }
                    break;
                }

                validMoves.add(new ChessMove(position, nextPos, null));
                x += dx;
                y += dy;
            }
        }
        return validMoves;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece myPiece = board.getPiece(myPosition);
        List<ChessMove> validMoves = new ArrayList<>();
        int[][] knightDirections = {
                {-1, 2},
                {1, 2},
                {-2, 1},
                {2, 1},
                {-2, -1},
                {2,-1},
                {-1,-2},
                {1,-2}
        };
        int[][] diagonalDirections = {
                {-1, 1}, // NW
                {1, 1}, // NE
                {-1, -1}, // SW
                {1, -1}  // SE
        };
        int[][] orthogonalDirections = {
                {0, 1}, // N
                {0, -1}, // S
                {1, 0}, // E
                {-1, 0}  // W
        };
        int[][] allDirections = Stream.concat(
                Arrays.stream(orthogonalDirections),
                Arrays.stream(diagonalDirections)
        ).toArray(int[][]::new);
        if (myPiece.getPieceType() == PieceType.BISHOP) {
            validMoves = loopGetMoves(validMoves, diagonalDirections, board, myPosition, myPiece);
        } else if (myPiece.getPieceType() == PieceType.KING) {
            validMoves = getMoves(validMoves, allDirections, board, myPosition, myPiece);
        } else if (myPiece.getPieceType() == PieceType.KNIGHT) {
            validMoves = getMoves(validMoves, knightDirections, board, myPosition, myPiece);
        } else if (myPiece.getPieceType() == PieceType.PAWN) {
            validMoves = getPawnMoves(validMoves, board, myPosition, myPiece);
        } else if (myPiece.getPieceType() == PieceType.QUEEN) {
            validMoves = loopGetMoves(validMoves, allDirections, board, myPosition, myPiece);
        } else if (myPiece.getPieceType() == PieceType.ROOK) {
            validMoves = loopGetMoves(validMoves, orthogonalDirections, board, myPosition, myPiece);
        }
        return validMoves;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceColor, type);
    }
}
