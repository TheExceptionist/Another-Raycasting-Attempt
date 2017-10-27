package net.theexceptionist.main;

import java.awt.Canvas;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import javax.swing.JFrame;

/**
 * 
 * @author kennethstepney
 *
 */

//I think I'm done for now
//Bye too anyone who actually watched all the way through
//I also won't copy this could, as I'm working from complete scratch, may not do things in the most efficient way
public class Main extends Canvas implements Runnable{
	private static final long serialVersionUID = 1628441814485809781L;
	public static final String TITLE = "Another Raycasting Attempt";
	public static final int WIDTH = 640;
	public static final int HEIGHT = 480;
	
	
	private boolean running = false;
	private Thread thread;
	private BufferedImage image = new BufferedImage(Main.WIDTH, Main.HEIGHT, BufferedImage.TYPE_INT_RGB);
	private int[] pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
	
	public static final int halfHeight = (Main.HEIGHT / 2);
	public static final int halfPixels = Main.halfHeight * Main.WIDTH;
	
	//Colors to render both the floor and ceiling pixels as.
	public final int ceilingColor = 0xFF0000FF;
	public final int floorColor = 0xFFAAAAAA;
	
	//each "tile" on the map has the dimensions 64 x 64
	public final int unitSize = 64;
	
	//Probably replace this later on
	public int[] map = {
		1, 1, 1, 1, 1, 1, 1, 1,	
		1, 0, 0, 0, 0, 0, 0, 1,	
		1, 0, 0, 0, 0, 0, 0, 1,	
		1, 0, 0, 0, 0, 0, 0, 1,	
		1, 0, 0, 0, 0, 0, 0, 1,	
		1, 0, 0, 0, 0, 0, 0, 1,	
		1, 0, 0, 0, 0, 0, 0, 1,	
		1, 0, 0, 0, 0, 0, 0, 1,	
		1, 0, 0, 0, 0, 0, 0, 1,	
		1, 1, 1, 1, 1, 1, 1, 1,	
	};
	
	//Will replace this with a player class later on as well.
	public double playerX = 1, playerY = 1;
	//Player rotation
	public double playerRot = angleToRad(60);
	//The height of the player
	public int playerHeight = 32;
	public double playerFOV = angleToRad(60);
	
	//Shouldn't need more that four keys for now.
	//0 - Up
	//1 - Down
	//2 - Right
	//3 - Left
	public boolean[] keys = new boolean[4];
	
	
	//640 x 480 Screen
	//halfHeight = 240
	//Center spot = 320, 240
	//tan(FOV) * 320 = distance from projectionPlane = 554
	//I'm thinking
	//Sorry for the wait
	//Actually I'll just put in the key input
	
	public static double angleToRad(double angle){
		return angle * (Math.PI / 180);
	}
	
	public void init(){
		requestFocus();
		
		this.addKeyListener(new KeyListener(){
			
			
			@Override
			public void keyTyped(KeyEvent e) {
				//Wrong method
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_UP) keys[0] = true;
				if(e.getKeyCode() == KeyEvent.VK_DOWN) keys[1] = true;
				if(e.getKeyCode() == KeyEvent.VK_RIGHT) keys[2] = true;
				if(e.getKeyCode() == KeyEvent.VK_LEFT) keys[3] = true;
			}

			@Override
			public void keyReleased(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_UP) keys[0] = false;
				if(e.getKeyCode() == KeyEvent.VK_DOWN) keys[1] = false;
				if(e.getKeyCode() == KeyEvent.VK_RIGHT) keys[2] = false;
				if(e.getKeyCode() == KeyEvent.VK_LEFT) keys[3] = false;
			}
			
		});
	}
	
	public void render(){
		BufferStrategy bs = getBufferStrategy();
		if(bs == null){
			createBufferStrategy(3);
			return;
		}
		
		boolean ceilRendered = renderCeil();
		boolean floorRendered = renderFloor();
		
		Graphics g = bs.getDrawGraphics();
		
		g.drawImage(image, 0, 0, Main.WIDTH, Main.HEIGHT, null);
		
		g.dispose();
		bs.show();
	}
	
	/**
	 * 
	 **/
	
	public boolean renderCeil(){
		boolean success = true;
		
		//pixels[0] = 0xFFFF0000;
		
		for(int x = 0; x < Main.WIDTH; x++){
			for(int y = 0; y < Main.HEIGHT; y++){
				//pixels[x + y * Main.WIDTH] = 0xFFFF0000;
				int yy = y * Main.WIDTH;
				
				if(x + yy < Main.halfPixels){
					pixels[x + yy] = ceilingColor;
				}
				//pixels[] = 0xFF0;
			}
		}
		
		
		return success;
	}
	
	public boolean renderFloor(){
		boolean success = true;
		
		//pixels[0] = 0xFFFF0000;
		
		for(int x = 0; x < Main.WIDTH; x++){
			for(int y = 0; y < Main.HEIGHT; y++){
				//pixels[x + y * Main.WIDTH] = 0xFFFF0000;
				int yy = (y * Main.WIDTH) + Main.halfPixels;
				
				if(x + yy > Main.halfPixels && x + yy < pixels.length){
					pixels[x + yy] = floorColor;
				}
				//pixels[] = 0xFF0;
			}
		}
		
		
		return success;
	}
	
	/**
	 * 
	 **/
	public void tick(){
		if(keys[0]){
			playerY++;
		}
		if(keys[1]){
			playerY--;
		}
		if(keys[2]){
			playerX++;
		}
		if(keys[3]){
			playerX--;
		}
		//System.out.println(playerX/64+" "+playerY/64);
	}
	
	public void start(){
		if(!running) running = true;
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop(){
		if(running) running = false;
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		long lastTime = System.nanoTime();
		double unprocessed = 0;
		double nsPerTick = 1000000000.0 / 60;
		int frames = 0;
		int ticks = 0;
		long lastTimer1 = System.currentTimeMillis();

		init();

		while (running) {
			long now = System.nanoTime();
			unprocessed += (now - lastTime) / nsPerTick;
			lastTime = now;
			boolean shouldRender = true;
			while (unprocessed >= 1) {
				ticks++;
				tick();
				unprocessed -= 1;
				shouldRender = true;
			}

			try {
				Thread.sleep(2);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (shouldRender) {
				frames++;
				render();
			}

			if (System.currentTimeMillis() - lastTimer1 > 1000) {
				lastTimer1 += 1000;
				System.out.println(ticks + " ticks, " + frames + " fps");
				frames = 0;
				ticks = 0;
			}
		}	
	}
	
	public static void main(String[] args){
		Main main = new Main();
		
		JFrame window = new JFrame(Main.TITLE);
		
		window.setSize(new Dimension(Main.WIDTH, Main.HEIGHT));
		
		window.add(main);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		window.setVisible(true);
		window.setResizable(false);
		window.setLocationRelativeTo(null);
		
		main.start();
	}

}
