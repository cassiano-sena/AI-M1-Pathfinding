import java.awt.*;
import java.awt.image.BufferedImage;

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

    public void calculaIA(int DiffTime) {
        vel = 0;
    }
}
