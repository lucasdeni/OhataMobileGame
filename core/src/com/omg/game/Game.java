package com.omg.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Game extends ApplicationAdapter {
	//Declaracao de variaveis
	private SpriteBatch batch;
	Texture img;

	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;

	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;

	private float larguraDispositivo;
	private float alturaDispositivo;
	private float variacao = 0;
	private float gravidade = 2;
	private float posicaoInicialVerticalPassaro = 0;
	private float posicaoCanoHorizontal;
	private float posicaoCanoVertical;
	private float espacoEntreCanos;
	private float posicaoHorizontalPassaro = 0;

	private int pontos = 0;
	private int pontuacaoMaxima = 0;
	private int estadoJogo = 0;

	private boolean passouCano = false;
	private Random random;

	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	Preferences preferencias;

	private OrthographicCamera camera;
	private Viewport viewport;
	private final float VIRTUAL_WIDTH = 720;
	private final float VIRTUAL_HEIGHT = 1280;

	//Metodo que inicia o app
	@Override
	public void create () {
		inicializarTexturas();
		inicializaObjetos();
	}


	private void inicializaObjetos() {
		batch = new SpriteBatch();
		random = new Random();

		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;

		//Inicializa textos
		textoPontuacao = new BitmapFont();
		textoPontuacao.setColor(Color.WHITE);
		textoPontuacao.getData().setScale(10);

		textoReiniciar = new BitmapFont();
		textoReiniciar.setColor(Color.GREEN);
		textoReiniciar.getData().setScale(2);

		textoMelhorPontuacao = new BitmapFont();
		textoMelhorPontuacao.setColor(Color.RED);
		textoMelhorPontuacao.getData().setScale(2);

		//Inicializa os formatos
		shapeRenderer = new ShapeRenderer();
		circuloPassaro = new Circle();
		retanguloCanoBaixo = new Rectangle();
		retanguloCanoCima = new Rectangle();

		//Inicializa os sons
		somVoando = Gdx.audio.newSound(Gdx.files.internal("som_asa.wav"));
		somColisao = Gdx.audio.newSound(Gdx.files.internal("som_batida.wav"));
		somPontuacao = Gdx.audio.newSound(Gdx.files.internal("som_pontos.wav"));

		//Inicializa as preferencias
		preferencias = Gdx.app.getPreferences("flappyBird");
		pontuacaoMaxima = preferencias.getInteger("pontuacaoMaxima", 0);

		//Inicializa as cameras
		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);

		camera = new OrthographicCamera();
		camera.position.set(VIRTUAL_WIDTH/2, VIRTUAL_HEIGHT/2, 0);
		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
	}

	//Metodo responsavel pelas texturas
	private void inicializarTexturas() {
		//Faz a animacao do passaro
		passaros = new Texture[3];
		passaros[0] = new Texture("passaro1.png");
		passaros[1] = new Texture("passaro2.png");
		passaros[2] = new Texture("passaro3.png");

		//Chamas as imagens do cenario
		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
	}

	//Chama esse metodo toda a vez que a rederizacao deve ser executada
	@Override
	public void render () {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		verificarEstadoJogo();
		validarPontos();
		desenharTexturas();
		detectarColisoes();
	}

	//Contabiliza os pontos ao passar um cano
	private void validarPontos() {
		if(posicaoCanoHorizontal < 50 - passaros[0].getWidth()){
			if(!passouCano){
				pontos++;
				passouCano = true;
				somPontuacao.play();
			}
		}
		variacao += Gdx.graphics.getDeltaTime() * 10;
		if(variacao > 3)
			variacao = 0;
	}

	private void desenharTexturas() {
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[ (int) variacao],
				50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo, posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical);
		batch.draw(canoTopo, posicaoCanoHorizontal,
				alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical );
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2,
				alturaDispositivo - 110);

		if(estadoJogo == 2){
			batch.draw(gameOver, larguraDispositivo / 2 - gameOver.getWidth() / 2,
					alturaDispositivo / 2);
			textoReiniciar.draw(batch,
					"Toque para reiniciar!", larguraDispositivo / 2 - 140,
					alturaDispositivo / 2 - gameOver.getHeight() / 2 );
			textoMelhorPontuacao.draw(batch,
					"Seu record é: "+ pontuacaoMaxima +" pontos",
					larguraDispositivo / 2 - 140, alturaDispositivo / 2 - gameOver.getHeight());
		}
		batch.end();
	}

	//Detecta as colisoes para o game over
	private void detectarColisoes() {
		circuloPassaro.set(
			50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2,
			posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2,
			passaros[0].getWidth() / 2
		);

		retanguloCanoBaixo.set(
			posicaoCanoHorizontal,
			alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoHorizontal,
			canoBaixo.getWidth(), canoBaixo.getHeight()
		);

		retanguloCanoCima.set(
			posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
			canoTopo.getWidth(), canoTopo.getHeight()
		);

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);

		if(colidiuCanoCima || colidiuCanoBaixo){
			if(estadoJogo == 1){
				somColisao.play();
				estadoJogo = 2;
			}
		}
	}

	private void verificarEstadoJogo() {
		boolean toqueTela = Gdx.input.justTouched(); //Verifica o toque na tela
		if(estadoJogo == 0){
			if(toqueTela){ //Comeca o jogo
				gravidade = -15;
				estadoJogo = 1;
				somVoando.play();
			}
		}else if (estadoJogo == 1){
			if(toqueTela){
				gravidade = -15;
				somVoando.play();
			}
			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200; //Movimenta a cam
			if(posicaoCanoHorizontal < -canoTopo.getWidth()){  //Define o local da abertura dos canos
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}
			if(posicaoInicialVerticalPassaro > 0 || toqueTela)  //Faz o passaro voar a cada toque
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			gravidade++;
		}else if(estadoJogo == 2){ 
			if(pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

			if(toqueTela){
				estadoJogo = 0;
				pontos = 0;
				gravidade = 0;
				posicaoHorizontalPassaro = 0;
				posicaoInicialVerticalPassaro = alturaDispositivo / 2;
				posicaoCanoHorizontal = larguraDispositivo;
			}
		}
	}

	//Chama sempre quando a tela do jogo e redimencionada e nao esta pausado
	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	//Chama quando o aplicativo e fechado
	@Override
	public void dispose () {

	}
}
