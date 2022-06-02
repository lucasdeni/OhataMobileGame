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
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.Random;

public class Game extends ApplicationAdapter {
	//Variaveis de imagem
	private SpriteBatch batch;
	private Texture[] passaros;
	private Texture fundo;
	private Texture canoBaixo;
	private Texture canoTopo;
	private Texture gameOver;
	private Texture inicio;
	private Texture coinOuro;
	private Texture coinPrata;

	//Variaveis dos formatos dos objetos
	private ShapeRenderer shapeRenderer;
	private Circle circuloPassaro;
	private Rectangle retanguloCanoCima;
	private Rectangle retanguloCanoBaixo;
	private Circle coinOuroCollider;
	private Circle coinPrataCollider;

	//Variaveis das funcoes do jogo
	private float coinPosHorizontalOuro;
	private float coinPosHorizontalPrata;
	private float coinPosVerticalOuro;
	private float coinPosVerticalPrata;
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

	//Texto
	BitmapFont textoPontuacao;
	BitmapFont textoReiniciar;
	BitmapFont textoMelhorPontuacao;

	//Som
	Sound somVoando;
	Sound somColisao;
	Sound somPontuacao;

	//Preferencias
	Preferences preferencias;

	//Controle de camera e tela
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

    //Metodo que inicia todos os objetos do jogo
	private void inicializaObjetos() {
		batch = new SpriteBatch();
		random = new Random();

		//Espacamento da tela
		larguraDispositivo = VIRTUAL_WIDTH;
		alturaDispositivo = VIRTUAL_HEIGHT;
		posicaoInicialVerticalPassaro = alturaDispositivo / 2;
		posicaoCanoHorizontal = larguraDispositivo;
		espacoEntreCanos = 350;
		coinPosHorizontalOuro = coinPosHorizontalOuro + larguraDispositivo / 2;
		coinPosHorizontalPrata = coinPosHorizontalPrata + larguraDispositivo / 2;
		coinPosVerticalOuro = random.nextInt((int) alturaDispositivo);
		coinPosVerticalPrata = random.nextInt((int) alturaDispositivo);

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
		coinOuroCollider = new Circle();
		coinPrataCollider = new Circle();

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
		passaros[2] = new Texture("passaro1.png");

		//Chamas as imagens do cenario
		fundo = new Texture("fundo.png");
		canoBaixo = new Texture("cano_baixo_maior.png");
		canoTopo = new Texture("cano_topo_maior.png");
		gameOver = new Texture("game_over.png");
		coinOuro = new Texture("coin_ouro.png");
		coinPrata = new Texture("coin_prata.png");
		inicio = new Texture("begin.png");
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

	//Metodo que faz aparecer os assets do jogo
	private void desenharTexturas() {
		batch.setProjectionMatrix(camera.combined); //Simula a camera
		batch.begin(); //Inicia o jogo
		//Desenha todas as texturas
		batch.draw(fundo, 0, 0, larguraDispositivo, alturaDispositivo);
		batch.draw(passaros[ (int) variacao],
				50 + posicaoHorizontalPassaro, posicaoInicialVerticalPassaro);
		batch.draw(canoBaixo,posicaoCanoHorizontal,alturaDispositivo/2-canoBaixo.getHeight()-espacoEntreCanos/2+posicaoCanoVertical);
		batch.draw(canoTopo,posicaoCanoHorizontal,alturaDispositivo/2+espacoEntreCanos/2+posicaoCanoVertical);
		batch.draw(coinOuro, coinPosHorizontalOuro, coinPosVerticalOuro);
		batch.draw(coinPrata, coinPosHorizontalPrata, coinPosVerticalPrata);
		textoPontuacao.draw(batch, String.valueOf(pontos), larguraDispositivo / 2,
				alturaDispositivo - 110);
		if(estadoJogo == 0){
			batch.draw(inicio, 0, 0, larguraDispositivo, alturaDispositivo);
		}

		//Responsavel pelos textos de pontuacao e outras informacoes
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

	//Detecta as colisoes do passaro
	private void detectarColisoes() {
		circuloPassaro.set(
			50 + posicaoHorizontalPassaro + passaros[0].getWidth() / 2,
			posicaoInicialVerticalPassaro + passaros[0].getHeight() / 2,
			passaros[0].getWidth() / 2
		);
		//Colisao do cano de baixo
		retanguloCanoBaixo.set(
				posicaoCanoHorizontal,
				alturaDispositivo / 2 - canoBaixo.getHeight() - espacoEntreCanos / 2 + posicaoCanoVertical,
				canoBaixo.getWidth(), canoBaixo.getHeight()
		);
		//Colisao do cano de cima
		retanguloCanoCima.set(
				posicaoCanoHorizontal, alturaDispositivo / 2 + espacoEntreCanos / 2 + posicaoCanoVertical,
				canoTopo.getWidth(), canoTopo.getHeight()
		);
		//Colisao da moeda de ouro
		coinOuroCollider.set(
				coinPosHorizontalOuro + coinOuro.getWidth() / 2,
				coinPosVerticalOuro + coinOuro.getHeight() / 2,
				coinOuro.getWidth() / 2
		);
		//Colisao da moeda de prata
		coinPrataCollider.set(
				coinPosHorizontalPrata + coinPrata.getWidth() / 2,
				coinPosVerticalPrata + coinPrata.getHeight() / 2,
				coinPrata.getWidth() / 2
		);

		boolean colidiuCanoCima = Intersector.overlaps(circuloPassaro, retanguloCanoCima);
		boolean colidiuCanoBaixo = Intersector.overlaps(circuloPassaro, retanguloCanoBaixo);
		boolean colidiuMoedaOuro = Intersector.overlaps(circuloPassaro, coinOuroCollider);
		boolean colidiuMoedaPrata = Intersector.overlaps(circuloPassaro, coinPrataCollider);

		if(colidiuMoedaOuro){
			somPontuacao.play();
			pontos += 10;
			coinPosHorizontalOuro = larguraDispositivo;
			coinPosVerticalOuro = random.nextInt((int) alturaDispositivo);
		}

		if(colidiuMoedaPrata){
			somPontuacao.play();
			pontos += 5;
			coinPosHorizontalPrata = larguraDispositivo;
			coinPosVerticalPrata = random.nextInt((int) alturaDispositivo);
		}

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
		}else if (estadoJogo == 1){ //Estado em que o jogador esta se movendo
			if(toqueTela){
				gravidade = -15;
				somVoando.play();
			}

			posicaoCanoHorizontal -= Gdx.graphics.getDeltaTime() * 200; //Gravidade no Cano
			if(posicaoCanoHorizontal < -canoTopo.getWidth()){  //Define o local da abertura dos canos
				posicaoCanoHorizontal = larguraDispositivo;
				posicaoCanoVertical = random.nextInt(400) - 200;
				passouCano = false;
			}

			coinPosHorizontalOuro -= Gdx.graphics.getDeltaTime() * 200; //Gravidade no Coin
			coinPosHorizontalPrata -= Gdx.graphics.getDeltaTime() * 200;
			if(coinPosHorizontalOuro <= -coinOuro.getWidth()){
				coinPosHorizontalOuro = larguraDispositivo;
				coinPosVerticalOuro = random.nextInt((int) alturaDispositivo);
			}

			if(coinPosHorizontalPrata <= -coinPrata.getWidth()){
				coinPosHorizontalPrata = larguraDispositivo;
				coinPosVerticalPrata = random.nextInt((int) alturaDispositivo);
			}

			if(posicaoInicialVerticalPassaro > 0 || toqueTela)  //Faz o passaro voar a cada toque
				posicaoInicialVerticalPassaro = posicaoInicialVerticalPassaro - gravidade;
			gravidade++;
		}else if(estadoJogo == 2){ //Estado em que o jogador morre e exibe sua pontuacao
			if(pontos > pontuacaoMaxima){
				pontuacaoMaxima = pontos;
				preferencias.putInteger("pontuacaoMaxima", pontuacaoMaxima);
				preferencias.flush();
			}
			posicaoHorizontalPassaro -= Gdx.graphics.getDeltaTime()*500;

			if(toqueTela){ //Recomeca o jogo
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
