package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    ChessBoard board = new ChessBoard();
    ChessGame.TeamColor teamColor;
    public ChessGame() {
        board.setStartingBoard();
        teamColor = TeamColor.WHITE;
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> moves = new ArrayList<>();
        ChessPiece piece = board.getPiece(startPosition);

        if (piece == null) {
            return null;
        } else {
            Collection<ChessMove> pieceMoves = piece.pieceMoves(board, startPosition);
            for (ChessMove move : pieceMoves) {
                // Simulate move
                ChessBoard simulationBoard = new ChessBoard(board);
                executePieceMove(simulationBoard, piece, move);

                // Add move if valid
                Boolean validMove = !isInCheckBoard(piece.getTeamColor(), simulationBoard);
                if (validMove) {
                    moves.add(move);
                }

                // Undo move (including potential capture)
                ChessMove undoMove = new ChessMove(move.getEndPosition(), move.getStartPosition(), null);
                executePieceMove(simulationBoard, piece, undoMove);
                simulationBoard.addPiece(move.getEndPosition(), board.getPiece(move.getEndPosition()));
            }
        }
        return moves;
    }

    public void executePieceMove(ChessBoard chessBoard, ChessPiece chessPiece, ChessMove chessMove) {
        chessBoard.addPiece(chessMove.getStartPosition(),null);
        ChessPiece.PieceType promPieceType = chessMove.getPromotionPiece();
        if (promPieceType != null) {
            ChessPiece promPiece = new ChessPiece(chessPiece.getTeamColor(), promPieceType);
            chessBoard.addPiece(chessMove.getEndPosition(), promPiece);
        } else {
            chessBoard.addPiece(chessMove.getEndPosition(), chessPiece);
        }
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        makeMoveBoard(move, board);
    }

    public void makeMoveBoard(ChessMove move, ChessBoard chessBoard) throws InvalidMoveException {
        ChessPosition moveStartPos = move.getStartPosition();
        ChessPiece pieceAtPos = chessBoard.getPiece(moveStartPos);

        Collection<ChessMove> valid_moves = validMoves(moveStartPos);
        if (pieceAtPos == null || pieceAtPos.getTeamColor() != teamColor || !valid_moves.contains(move)) {
            throw new InvalidMoveException();
        } else {
            // Execute move
            executePieceMove(chessBoard, pieceAtPos, move);

            // Switch turn
            TeamColor otherTeamColor = pieceAtPos.getTeamColor() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
            setTeamTurn(otherTeamColor);
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheckBoard(teamColor, board);
    }

    public boolean isInCheckBoard(TeamColor teamColor, ChessBoard chessBoard) {
        Collection<ChessPosition> kings = chessBoard.getPiecePositions(teamColor, ChessPiece.PieceType.KING);
        ChessPosition kingPos = null;

        if (!kings.isEmpty()) {
            kingPos = kings.iterator().next();
        }

        TeamColor oppTeamColor = teamColor == TeamColor.BLACK ? TeamColor.WHITE : TeamColor.BLACK;
        Collection<ChessPosition> oppTeamPositions = chessBoard.getTeamPiecePositions(oppTeamColor);

        for (ChessPosition oppPos : oppTeamPositions) {
            ChessPiece oppPiece = chessBoard.getPiece(oppPos);
            Collection<ChessMove> oppPieceMoves = oppPiece.pieceMoves(chessBoard, oppPos);
            for (ChessMove move : oppPieceMoves) {
                ChessPosition endPos = move.getEndPosition();
                if (endPos.equals(kingPos)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        ChessBoard simulationBoard = new ChessBoard(board);

        if (!isInCheckBoard(teamColor, simulationBoard)) {
            return false;
        }
        Collection<ChessPosition> teamPositions = simulationBoard.getTeamPiecePositions(teamColor);

        for (ChessPosition piecePos : teamPositions) {
            ChessPiece piece = simulationBoard.getPiece(piecePos);
            Collection<ChessMove> pieceMoves = piece.pieceMoves(simulationBoard, piecePos);

            for (ChessMove move : pieceMoves) {
                executePieceMove(simulationBoard, piece, move);
                if (!isInCheckBoard(teamColor, simulationBoard)) {
                    return false;
                }
                // Undo move (including potential capture)
                ChessMove undoMove = new ChessMove(move.getEndPosition(), move.getStartPosition(), null);
                executePieceMove(simulationBoard, piece, undoMove);
                simulationBoard.addPiece(move.getEndPosition(), board.getPiece(move.getEndPosition()));
            }
        }
        return true;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return false;
        }

        Collection<ChessPosition> teamPositions = board.getTeamPiecePositions(teamColor);

        for (ChessPosition piecePos : teamPositions) {
            Collection<ChessMove> valid_moves = validMoves(piecePos);
            if (!valid_moves.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard passedBoard) {
        board = passedBoard;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && teamColor == chessGame.teamColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamColor);
    }
}
