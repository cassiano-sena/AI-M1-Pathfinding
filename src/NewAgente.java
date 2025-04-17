/**
 * PERSONAGEM COM SPRITE CARREGAVEL
 */

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Arrays;

public class NewAgente extends Agente {

    Color color;
    double vel = 40;
    double ang = 0;

    int estado = 0;
    double oldx = 0, oldy = 0;
    int timeria = 0;
    boolean colidiu = false;

    Personagem sprite;  // <- Add this
    BufferedImage charaSheet;

    public NewAgente(int x, int y, BufferedImage charaSprite) {
        X = x;
        Y = y;
        this.charaSheet = charaSprite;
        sprite = new Personagem(charaSprite);  // <- Create personagem
        sprite.X = X;
        sprite.Y = Y;
    }

    @Override
    public void SimulaSe(int DiffTime) {
        timeria += DiffTime;

        oldx = X;
        oldy = Y;

        if (timeria > 100) {
            calculaIA(DiffTime);
            timeria = 0;
        }

        X += Math.cos(ang) * vel * DiffTime / 1000.0;
        Y += Math.sin(ang) * vel * DiffTime / 1000.0;

        // Sync the internal sprite's position
        sprite.X = X;
        sprite.Y = Y;

        for (Agente agente : GamePanel.listadeagentes) {
            if (agente != this) {
                double dax = agente.X - X;
                double day = agente.Y - Y;
                double dista = dax * dax + day * day;

                if (dista < 400) {
                    X = oldx;
                    Y = oldy;
                    colidiu = true;
                    break;
                }
            }
        }

        sprite.SimulaSe(DiffTime); // <- Call sprite simulation
    }

    @Override
    public void DesenhaSe(Graphics2D dbg, int XMundo, int YMundo) {
        sprite.DesenhaSe(dbg, XMundo, YMundo); // <- Draw animated sprite
    }
    // funcao DesenhaSe melhor adaptada pro zoom
    public void drawAgente(Graphics2D dbg, int XMundo, int YMundo, float zoom) {
        dbg.setColor(color);
        dbg.drawOval((int) ((int)((X-10)-XMundo)*zoom), (int) ((int)((Y-10)-YMundo)*zoom), (int) (20*zoom), (int) (20*zoom));
        double linefx = X + 10*Math.cos(ang);
        double linefy = Y + 10*Math.sin(ang);dbg.drawLine((int) ((int)(X-XMundo)*zoom), (int) ((int)(Y-YMundo)*zoom), (int) ((int)(linefx-XMundo)*zoom), (int) ((int)(linefy-YMundo)*zoom));
    }

    public void moveAgente(int[] caminho) throws InterruptedException {
        int[][] caminhoAtual = new int[caminho.length / 2][2];
        for (int i = 0; i < caminho.length; i += 2) {
            int index = i / 2;
            caminhoAtual[index][0] = caminho[i];     // X
            caminhoAtual[index][1] = caminho[i + 1]; // Y
        }
        System.out.println(Arrays.deepToString(caminhoAtual));
        for (int[] grid : caminhoAtual) {
            int gridX = grid[0]*16+8;
            int gridY = grid[1]*16+8;
            this.X = gridX;
            this.Y = gridY;
            try{
                Thread.sleep(50);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void calculaIA(int DiffTime) {
        vel = 0;
    }
}
