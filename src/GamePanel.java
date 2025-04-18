/**
 * PAINEL DE JOGO
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;

import javax.swing.*;
import java.io.*;
import java.util.*;
import java.awt.image.*;
import javax.imageio.ImageIO;


public class GamePanel extends Canvas implements Runnable
{
private static final int PWIDTH = 960;
private static final int PHEIGHT = 800;
private Thread animator;
private boolean running = false;
private boolean gameOver = false;


int FPS,SFPS;
int fpscount;

public static Random rnd = new Random();


boolean LEFT, RIGHT,UP,DOWN;

public static int mousex,mousey;

	/**
	 * NewAgente = Sprite
	 * MeuAgente = Bolinha
	 */
	BufferedImage imagemcharsets;

	public static ArrayList<MeuAgente> listadeagentes = new ArrayList<MeuAgente>();
//	public static ArrayList<NewAgente> listadeagentes = new ArrayList<NewAgente>();

	MeuAgente newHeroi = null;
//	NewAgente newHeroi = null;

Mapa_Grid mapa;

double posx,posy;


//TODO ESSE È O RESULTADO
int caminho[] = null;

float zoom = 1;

int ntileW = 60;
int ntileH = 50;

Font f = new Font("", Font.BOLD, 20);

	private static class AStarNode implements Comparable<AStarNode> {
		int x, y;
		int g; // cost from start
		int h; // heuristic cost to goal
		int f; // total cost (g + h)
		AStarNode parent;

		AStarNode(int x, int y, AStarNode parent, int g, int h) {
			this.x = x;
			this.y = y;
			this.parent = parent;
			this.g = g;
			this.h = h;
			this.f = g + h;
		}

		@Override
		public int compareTo(AStarNode other) {
			return this.f - other.f;
		}
	}

	// Manhattan distance heuristic (no diagnoals)
	private int heuristic(int x, int y, int goalX, int goalY) {
		return Math.abs(x - goalX) + Math.abs(y - goalY);
	}

	//LinkedList<Nodo> nodosPercorridos = new LinkedList();
	HashSet<Integer> nodosPercorridos = new HashSet<Integer>();

	public boolean jaPassei(int nX,int nY) {
		return nodosPercorridos.contains(nX + nY * 1000);
	}

	// A* pathfinding method.
	// Returns true if a path is found (storing it in the variable "caminho"), false otherwise.
	public boolean aStarPathfinding(int startX, int startY, int goalX, int goalY) {
		PriorityQueue<AStarNode> openSet = new PriorityQueue<>();
		HashSet<String> closedSet = new HashSet<>();

		AStarNode startNode = new AStarNode(startX, startY, null, 0, heuristic(startX, startY, goalX, goalY));
		openSet.add(startNode);

		while (!openSet.isEmpty()) {
			AStarNode current = openSet.poll();

			if (current.x == goalX && current.y == goalY) {
				// Reconstruct path
				LinkedList<AStarNode> pathList = new LinkedList<>();
				AStarNode temp = current;
				while (temp != null) {
					pathList.addFirst(temp);
					temp = temp.parent;
				}
				// Allocate the variable "caminho" array:
				caminho = new int[pathList.size() * 2];
				int i = 0;
				for (AStarNode n : pathList) {
					caminho[i++] = n.x;
					caminho[i++] = n.y;
				}
				return true;
			}

			closedSet.add(current.x + "," + current.y);

			// 4-directional neighbors (down left up right).
			int[][] directions = { {0, 1}, {1, 0}, {0, -1}, {-1, 0} };
			for (int[] d : directions) {
				int nx = current.x + d[0];
				int ny = current.y + d[1];

				// Bounds check
				if (nx < 0 || ny < 0 || nx >= mapa.Largura || ny >= mapa.Altura)
					continue;
				// Check if walkable (0 means walkable)
				if (mapa.mapa[ny][nx] != 0)
					continue;
				if (closedSet.contains(nx + "," + ny))
					continue;

				int tentativeG = current.g + 1; // constant cost
				int h = heuristic(nx, ny, goalX, goalY);
				AStarNode neighbor = new AStarNode(nx, ny, current, tentativeG, h);
				openSet.add(neighbor);
				synchronized (nodosPercorridos) {
					nodosPercorridos.add(nx + ny * 1000);
				}
			}
		}
		return false;
	}

	// ***** End A* *****

	public GamePanel()
	{

		setBackground(Color.white);
		setPreferredSize( new Dimension(PWIDTH, PHEIGHT));

		// create game components
		setFocusable(true);

		requestFocus(); // JPanel now receives key events


		// Adiciona um Key Listner
		addKeyListener( new KeyAdapter() {
			public void keyPressed(KeyEvent e)
			{
				int keyCode = e.getKeyCode();

				if(keyCode == KeyEvent.VK_LEFT){
					LEFT = true;
				}
				if(keyCode == KeyEvent.VK_RIGHT){
					RIGHT = true;
				}
				if(keyCode == KeyEvent.VK_UP){
					UP = true;
				}
				if(keyCode == KeyEvent.VK_DOWN){
					DOWN = true;
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				int keyCode = e.getKeyCode();

				if(keyCode == KeyEvent.VK_LEFT){
					LEFT = false;
				}
				if(keyCode == KeyEvent.VK_RIGHT){
					RIGHT = false;
				}
				if(keyCode == KeyEvent.VK_UP){
					UP = false;
				}
				if(keyCode == KeyEvent.VK_DOWN){
					DOWN = false;
				}
			}
		});

		addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				mousex = e.getX();
				mousey = e.getY();
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				if(e.getButton()==3){
					int mousex = (int)(e.getX() / zoom) + mapa.MapX;
					int mousey = (int)(e.getY() / zoom) + mapa.MapY;

					int mx = mousex/16;
					int my = mousey/16;

					if(mx>mapa.Altura) {
						return;
					}
					if(my>mapa.Largura) {
						return;
					}

					mapa.mapa[my][mx] = 1;
				}
			}
		});

		addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent arg0) {
				// TODO Auto-generated method stub
				//System.out.println(" "+arg0.getButton());
				int mousex = (int)(arg0.getX() / zoom) + mapa.MapX;
				int mousey = (int)(arg0.getY() / zoom) + mapa.MapY;

				//System.out.println(""+arg0.getX()+" "+mapa.MapX+" "+zoom);
				//System.out.println(""+mousex+" "+mousey);

				int mx = mousex/16;
				int my = mousey/16;

				if(mx>mapa.Altura) {
					return;
				}
				if(my>mapa.Largura) {
					return;
				}

				if(arg0.getButton()==3){


					if(mapa.mapa[my][mx]==0){
						mapa.mapa[my][mx] = 1;
					}else{
						mapa.mapa[my][mx] = 0;
					}
				}

				//****************************************************
				//new (a*)
				//****************************************************
				if(arg0.getButton() == 1) {
					if(mapa.mapa[my][mx] == 0) {
						caminho = null;
						long timeini = System.currentTimeMillis();

						System.out.println("Target tile: " + mx + " " + my);
						System.out.println("Herói:  " + (int)(newHeroi.X/16) + " " + (int)(newHeroi.Y/16));
						boolean found = aStarPathfinding((int)(newHeroi.X/16), (int)(newHeroi.Y/16), mx, my);
						if(found) {
							System.out.println("Path found!");
						} else {
							System.out.println("Path not found.");
						}
						long timefin = System.currentTimeMillis() - timeini;
						System.out.println("Tempo Final: " + timefin + "ms.");
						try {
							if (caminho != null) newHeroi.moveAgente(caminho);
							caminho = null; // clean path after
							nodosPercorridos.clear();
						} catch (InterruptedException e) {
							throw new RuntimeException(e);
						}
					} else {
						System.out.println("Caminho Final Bloqueado");
					}
				}
				//****************************************************
			}

			@Override
			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}

			@Override
			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub
			}
		});

		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if(e.getWheelRotation()>0) {
					zoom *= 0.9f;
				}else if(e.getWheelRotation()<0) {
					zoom *= 1.1f;
				}

				ntileW = (int)((960/zoom)/16)+1;
				ntileH = (int)((800/zoom)/16)+1;

				if(ntileW>=1000) {
					ntileW = 1000;
				}
				if(ntileH>=1000) {
					ntileH = 1000;
				}
				mapa.NumeroTilesX = ntileW;
				mapa.NumeroTilesY = ntileH;

//				System.out.println(zoom + ": w "+e.getWheelRotation());
			}
		});

//************************************************************
		/**
		 * Para Personagem com sprite
		 * lembrar de mudar o tipo da variavel no cabecalho do codigo
		 */
//		try {
//			imagemcharsets = ImageIO.read(getClass().getResourceAsStream("/Chara1.png"));
//		} catch (IOException e) {
//			throw new RuntimeException(e);
//		}
//		newHeroi = new NewAgente(8, 8, imagemcharsets);
//************************************************************

		/**
		 * Para Personagem bolinha azul
		 * lembrar de mudar o tipo da variavel no cabecalho do codigo
		  */
		newHeroi = new MeuAgente(8, 8, Color.CYAN);
//************************************************************
		listadeagentes.add(newHeroi);

		mousex = mousey = 0;

		mapa = new Mapa_Grid(100,100,ntileW, ntileH);
		mapa.loadmapfromimage("/imagemlabirinto1000.png");

	} // end of GamePanel()


LinkedList<Nodo> pilhaprofundidade = new LinkedList();

public void startGame()
// initialise and start the thread
{
	if (animator == null || !running) {
		animator = new Thread(this);
		animator.start();
	}
} // end of startGame()

public void stopGame()
// called by the user to stop execution
{ running = false; }


public void run()
/* Repeatedly update, render, sleep */
{
	running = true;

	long DifTime,TempoAnterior;

	int segundo = 0;
	DifTime = 0;
	TempoAnterior = System.currentTimeMillis();

	this.createBufferStrategy(2);
	BufferStrategy strategy = this.getBufferStrategy();

	while(running) {

		gameUpdate(DifTime); // game state is updated
		Graphics g = strategy.getDrawGraphics();
		gameRender((Graphics2D)g); // render to a buffer
		strategy.show();

		try {
			Thread.sleep(0); // sleep a bit
		}
		catch(InterruptedException ex){}

		DifTime = System.currentTimeMillis() - TempoAnterior;
		TempoAnterior = System.currentTimeMillis();

		if(segundo!=((int)(TempoAnterior/1000))){
			FPS = SFPS;
			SFPS = 1;
			segundo = ((int)(TempoAnterior/1000));
		}else{
			SFPS++;
		}

	}
System.exit(0); // so enclosing JFrame/JApplet exits
} // end of run()

int timerfps = 0;
private void gameUpdate(long DiffTime)
{
	// System.out.println(posx + " | " + posy);
	if (DiffTime < 1) DiffTime = 1;

	if(LEFT){
		posx-=1000*DiffTime/1000.0;
	}
	if(RIGHT){
		posx+=1000*DiffTime/1000.0;
	}
	if(UP){
		posy-=1000*DiffTime/1000.0;
	}
	if(DOWN){
		posy+=1000*DiffTime/1000.0;
	}

	if(posx>mapa.Largura*16) {
		posx=mapa.Largura*16;
	}
	if(posy>mapa.Altura*16) {
		posy=mapa.Altura*16;
	}
	if(posx<0) {
		posx=0;
	}
	if(posy<0) {
		posy=0;
	}

	mapa.Posiciona((int)posx,(int)posy);

	for(int i = 0;i < listadeagentes.size();i++){
		  listadeagentes.get(i).SimulaSe((int)DiffTime);
	}
}


private void gameRender(Graphics2D dbg)
// draw the current frame to an image buffer
{
    // clear the background
    dbg.setColor(Color.WHITE);
    dbg.fillRect(0, 0, PWIDTH, PHEIGHT);

    AffineTransform trans = dbg.getTransform();
    dbg.scale(zoom, zoom);

    try {
        mapa.DesenhaSe(dbg);
    } catch (Exception e) {
        System.out.println("Erro ao desenhar mapa");
    }

//    for (int i = 0; i < listadeagentes.size(); i++) {
//        listadeagentes.get(i).DesenhaSe(dbg, mapa.MapX, mapa.MapY);
//    }

    // mouse hover highlight
    int tileSize = 16;
    int worldMouseX = (int)(mousex / zoom) + mapa.MapX;
    int worldMouseY = (int)(mousey / zoom) + mapa.MapY;
    int tileX = worldMouseX / tileSize;
    int tileY = worldMouseY / tileSize;

    if(tileX >= 0 && tileX < mapa.Largura && tileY >= 0 && tileY < mapa.Altura) {
        if(mapa.mapa[tileY][tileX] == 0) {
            dbg.setColor(new Color(255, 255, 0, 128));
            dbg.fillRect(tileX * tileSize - mapa.MapX, tileY * tileSize - mapa.MapY, tileSize, tileSize);
        }
    }
    // ---- End highlight ----

	if (nodosPercorridos != null) {
		HashSet<Integer> snapshot;
		synchronized (nodosPercorridos) {
			snapshot = new HashSet<Integer>(nodosPercorridos);
		}
		try {
			for (Iterator iterator = snapshot.iterator(); iterator.hasNext(); ) {
				Integer nxy = (Integer) iterator.next();
				int px = nxy % 1000;
				int py = (int) (nxy / 1000);
				dbg.setColor(Color.GREEN);
				dbg.fillRect(px * 16 - mapa.MapX, py * 16 - mapa.MapY, 16, 16);
			}
		} catch (Exception e) { }
	}

    if(caminho != null) {
        try {
            for (int i = 0; i < caminho.length / 2; i++) {
                int nx = caminho[i * 2];
                int ny = caminho[i * 2 + 1];
                dbg.setColor(Color.BLUE);
                dbg.fillRect(nx * 16 - mapa.MapX, ny * 16 - mapa.MapY, 16, 16);
            }
        } catch (Exception e) {
        }
    }

    dbg.setTransform(trans);

    dbg.setFont(f);
    dbg.setColor(Color.GREEN);
    dbg.drawString("FPS: " + FPS, 10, 30);
    dbg.drawString("N: " + nodosPercorridos.size(), 100, 30);
    // System.out.println("left " + LEFT);7
	for (int i = 0; i < listadeagentes.size(); i++) {
		listadeagentes.get(i).drawAgente(dbg, mapa.MapX, mapa.MapY, zoom);
	}
}
}