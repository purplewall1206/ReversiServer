package cc.cxsj.nju.reversi.chess;

import cc.cxsj.nju.reversi.ui.MainFrame;
import org.apache.log4j.Logger;

import cc.cxsj.nju.reversi.Main;
import cc.cxsj.nju.reversi.config.ServerProperties;

import java.util.IntSummaryStatistics;

public class ChessBoard {
	private static final Logger LOG = Logger.getLogger(Main.class);
	private static final int ROWS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.rows"));
	private static final int COLS = Integer.valueOf(ServerProperties.instance().getProperty("chess.board.cols"));
	private static final int INTERVAL = Integer.valueOf(ServerProperties.instance().getProperty("play.interval"));
	private static String spliter = "--------------------------------------------";
    private int[] dx = new int[]{0, 1, 1,  1, 0, -1, -1, -1};
    private int[] dy = new int[]{1, 1, 0, -1, -1, -1, 0, 1};
	
	// the chess board
	private Square[][] board = new Square[ROWS][COLS];
    private int lastStepRow = -1, lastStepCol = -1;

	public ChessBoard() {}

	public void generateEmptyChessBoard() {
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
					this.board[i][j] = new Square(-1);
            }
        }
        MainFrame.instance().ClearChessBoardUI();
    }

    public boolean inBoard(int row, int col) {
        return (row >= 0 && row < ROWS && col >=0 && col < COLS);
    }

	/**
	 * 
	 * @param step
	 * @param color 0 is black, 1 is white
	 * @return returnCode R code P desRow desCol.
	 * 		   code 0 : sucess;
	 * 		   code 1 : msg format error;
	 *         code 2 : coordinate error;
	 *         code 3 : color error;
	 *         code 4 : invalid step.
	 */
    public int step(int x, int y, int stepNum, int color){
    	int returnCode = 0;
    	
    	if(board[x][y].color == -1 && canLazi(x,y,color)){
    		board[x][y].color = color;
    		return 0;
    	}
    	
    	
    	
    	return returnCode;
    }
    
	public String step(String step, int stepNum, int color) {
		// System.out.println("Handle Step " + step + " " + color);
		// check step or not
		if (step.charAt(0) != 'S') {  // S represents step
            // System.out.println("Step.charAt(0) != S");
            return "R1";
        }
		
		switch (step.charAt(1)) {
            case 'P':
            {
                // put down the piece
                for (int i = 2; i < 6; i++) {
                    if (step.charAt(i) > '9' || step.charAt(i) < '0')
                        return "R2";
                }
                int desRow = Integer.valueOf(step.substring(2, 4)), desCol = Integer.valueOf(step.substring(4, 6));
                if (desRow >= ROWS || desRow < 0) return "R2";
                if (desCol >= COLS || desCol < 0) return "R2";
                Square desSquare = this.board[desRow][desCol];

                if (desSquare.empty) {
                    // update step result
                    desSquare.empty = false;
                    desSquare.color = color;
                    MainFrame.instance().updateStepInfo((color==0?"Black ":"White ")+step.substring(0, 6), stepNum);
                    MainFrame.instance().updateChessBoardUI(lastStepRow, lastStepCol, desRow, desCol, color);
                    lastStepRow = desRow;
                    lastStepCol = desCol;
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "R0" + step.substring(1, 6) + desSquare.color;
                } else {
                    System.out.println("Put Down ERROR");
                    for (int i=0; i<15; i++) {
                        for (int j=0; j<15; j++) {
                            if (this.board[i][j].color != -1)
                                System.out.print(this.board[i][j].color + " ");
                            else
                                System.out.print("-" + " ");
                        }
                        System.out.println();
                    }
                    return "R4";
                }
            }
            case 'D':
            {
                // put down the piece
                for (int i = 2; i < 6; i++) {
                    if (step.charAt(i) > '9' || step.charAt(i) < '0')
                        return "R2";
                }
                int desRow = Integer.valueOf(step.substring(2, 4)), desCol = Integer.valueOf(step.substring(4, 6));
                if (desRow >= ROWS || desRow < 0) return "R2";
                if (desCol >= COLS || desCol < 0) return "R2";
                Square desSquare = this.board[desRow][desCol];

                if (!desSquare.empty) {
                    // update step result
                    desSquare.empty = true;
                    desSquare.color = -1;
                    MainFrame.instance().updateStepInfo((color==0?"Black ":"White ")+step.substring(0, 6), stepNum);
                    MainFrame.instance().updateChessBoardUI(lastStepRow, lastStepCol, desRow, desCol, -1);
                    lastStepRow = desRow;
                    lastStepCol = desCol;
                    try {
                        Thread.sleep(INTERVAL);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return "R0" + step.substring(1, 6) + color;
                } else {
                    System.out.println("Disappear ERROR");
                    return "R4";
                }
            }
            case 'N': {   // Nostep
                return "R0N";
            }
            default:
                return "R1";
		}
	}

	/**
	 * exist an position that can reversi chessman
	 *  
	 */
	public boolean isGameEnd(){
		boolean gameEnd = true;
		
		for(int x = 0; x < ROWS; x ++){
			for(int y = 0; y < COLS; y ++){
				if(! (board[x][y].existChessman() || !canLazi(x,y,0) || !canLazi(x,y,1))){
					return false;
					
				}
			}
		}
		
		return gameEnd;
	}
	
	/**
	 * can player put chessman on the position (x,y)
	 * 
	 */
	public boolean canLazi(int x, int y, int chessmanColor){
		boolean lazi = true;
		
		//if the position (x,y) is not empty
		if(board[x][y].color != -1){
			lazi = false;
		}
		//
		else{
			//travel all direction of position (x,y) to find the chessman confirm to reversi rules 
			for(int dir = 0; dir < dx.length; dir ++){
				int pos_x = x + dx[dir], pos_y = y + dy[dir];
				int color = chessmanColor;
				
				boolean opposite = false;
				boolean reversi = false;
				
				//travel neighbor in direction dir
				while(inBoard(pos_x, pos_y) && !reversi){

					if(!board[pos_x][pos_y].existChessman()){
						break;
					}
					//if the color of neighbor position is the opposite color of player 
					if(board[pos_x][pos_y].color == 1-color){
						opposite = true;
					}
					//if the color of neighbor position is the same color of player 
					//&& there is no opposite color chessman between this position and target position
					else if(!opposite){
						break;
					}
					//if the color of neighbor position is the same color of player 
					//&& there is some opposite color chessman between this position and target position
					else{
						reversi = true;
						break;
					}
				}
				
				//can reversi in an direction 
				if(reversi){
					lazi = true;
					break;
				}
			}
		}
		

		return lazi;
	}
	
	
	/**
	 * -1 has not winnner, 0 winner is black, 1 winner is white, 2 is draw
	 * 
	 * @return
	 */
	public int isGeneratedWinner(){
		if(isGameEnd()){
			int blackCount = 0, whiteCount = 0;
			
			//count black chessman and white chessman
			for(int x = 0; x < ROWS; x ++){
				for(int y = 0; y < COLS; y ++){
					if(board[x][y].color == 0){
						blackCount ++;
					}
					else if(board[x][y].color == 1){
						whiteCount ++;
					}
				}
			}
			
			if(blackCount > whiteCount){
				return 0;
			}
			else if(blackCount < whiteCount){
				return 1;
			}
			else{
				return 2;
			}
		}
		return -1;
	}
	
	
	/*public int isGeneratedWinnner() {
		int sR = lastStepRow;
        int sC = lastStepCol;
        int nowColor = -1;
        if (inBoard(sR, sC) && board[sR][sC].color == -1) return -1;
        else if(inBoard(sR, sC)) nowColor = board[sR][sC].color;
        int MaxSeq = 1;
        for (int dir = 0; dir < 4; dir++) {       // 4 directions and their opposite directions
            int seq = 1;
            boolean d0 = true, d1 = true;
            for (int len = 1; len <= 4; len++) {  // walk at most 4 steps
                int deltax = dx[dir]*len;
                int deltay = dy[dir]*len;
                if (d0 && inBoard(sR+deltax, sC+deltay) && board[sR+deltax][sC+deltay].color == nowColor)
                    seq++;
                else
                    d0 = false;
                if (d1 && inBoard(sR-deltax, sC-deltay) && board[sR-deltax][sC-deltay].color == nowColor)
                    seq++;
                else
                    d1 = false;
                MaxSeq = seq > MaxSeq ? seq:MaxSeq;
            }
        }
        if (MaxSeq >= 5) {
            System.out.println("Winner is " + (nowColor==0?"Black":"White"));
            return nowColor;
        }
		return -1;
	}*/

	public String toStringToDisplay() {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder(spliter);
		sb.append("\n");
		for (int i = 0; i < ROWS; i++) {
			for (int j = 0; j < COLS; j++) {
				sb.append(board[i][j].toStringToDisplay());
				sb.append(" ");
			}
			sb.append("\n");
		}
		sb.append(spliter);
		return sb.toString();
	}
}
