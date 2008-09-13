/**
 * simpleLiteの複数マーカー同時認識バージョン
 * "Hiro"のマーカーと"人"のマーカーの混在環境で、Hiroのマーカー全てに
 * 立方体を表示します。
 * (c)2008 A虎＠nyatla.jp
 * airmail(at)ebony.plala.or.jp
 * http://nyatla.jp/
 */
package jp.nyatla.nyartoolkit.jogl.sample;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;

import javax.media.Buffer;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.GLCanvas;

import com.sun.opengl.util.Animator;

import jp.nyatla.nyartoolkit.core.NyARCode;

import jp.nyatla.nyartoolkit.jmf.utils.JmfCameraCapture;
import jp.nyatla.nyartoolkit.jmf.utils.JmfCaptureListener;
import jp.nyatla.nyartoolkit.jogl.utils.*;

public class JavaSimpleLite2 implements GLEventListener, JmfCaptureListener
{
	private final String CARCODE_FILE1 = "../../Data/patt.hiro";

	private final String CARCODE_FILE2 = "../../Data/patt.kanji";

	private final String PARAM_FILE = "../../Data/camera_para.dat";

	private final static int SCREEN_X = 640;

	private final static int SCREEN_Y = 480;

	private Animator _animator;

	private GLNyARRaster_RGB _cap_image;

	private JmfCameraCapture _capture;

	private GL _gl;

	private NyARGLUtil _glnya;

	//NyARToolkit関係
	private GLNyARDetectMarker _nya;

	private GLNyARParam _ar_param;

	/**
	 * 立方体を書く
	 *
	 */
	void drawCube()
	{
		// Colour cube data.
		int polyList = 0;
		float fSize = 0.5f;//マーカーサイズに対して0.5倍なので、4cmのナタデココ
		int f, i;
		float[][] cube_vertices = new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, -1.0f, 1.0f }, { -1.0f, -1.0f, 1.0f }, { -1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, -1.0f }, { 1.0f, -1.0f, -1.0f }, { -1.0f, -1.0f, -1.0f }, { -1.0f, 1.0f, -1.0f } };
		float[][] cube_vertex_colors = new float[][] { { 1.0f, 1.0f, 1.0f }, { 1.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 0.0f }, { 0.0f, 1.0f, 1.0f }, { 1.0f, 0.0f, 1.0f }, { 1.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 0.0f }, { 0.0f, 0.0f, 1.0f } };
		int cube_num_faces = 6;
		short[][] cube_faces = new short[][] { { 3, 2, 1, 0 }, { 2, 3, 7, 6 }, { 0, 1, 5, 4 }, { 3, 0, 4, 7 }, { 1, 2, 6, 5 }, { 4, 5, 6, 7 } };

		if (polyList == 0) {
			polyList = _gl.glGenLists(1);
			_gl.glNewList(polyList, GL.GL_COMPILE);
			_gl.glBegin(GL.GL_QUADS);
			for (f = 0; f < cube_num_faces; f++)
				for (i = 0; i < 4; i++) {
					_gl.glColor3f(cube_vertex_colors[cube_faces[f][i]][0], cube_vertex_colors[cube_faces[f][i]][1], cube_vertex_colors[cube_faces[f][i]][2]);
					_gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
				}
			_gl.glEnd();
			_gl.glColor3f(0.0f, 0.0f, 0.0f);
			for (f = 0; f < cube_num_faces; f++) {
				_gl.glBegin(GL.GL_LINE_LOOP);
				for (i = 0; i < 4; i++)
					_gl.glVertex3f(cube_vertices[cube_faces[f][i]][0] * fSize, cube_vertices[cube_faces[f][i]][1] * fSize, cube_vertices[cube_faces[f][i]][2] * fSize);
				_gl.glEnd();
			}
			_gl.glEndList();
		}

		_gl.glPushMatrix(); // Save world coordinate system.
		_gl.glTranslatef(0.0f, 0.0f, 0.5f); // Place base of cube on marker surface.
		_gl.glRotatef(0.0f, 0.0f, 0.0f, 1.0f); // Rotate about z axis.
		_gl.glDisable(GL.GL_LIGHTING); // Just use colours.
		_gl.glCallList(polyList); // Draw the cube.
		_gl.glPopMatrix(); // Restore world coordinate system.

	}

	public JavaSimpleLite2()
	{
		Frame frame = new Frame("Java simpleLite with NyARToolkit");

		// 3Dを描画するコンポーネント
		GLCanvas canvas = new GLCanvas();
		frame.add(canvas);
		canvas.addGLEventListener(this);
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		frame.setVisible(true);
		Insets ins = frame.getInsets();
		frame.setSize(SCREEN_X + ins.left + ins.right, SCREEN_Y + ins.top + ins.bottom);
		canvas.setBounds(ins.left, ins.top, SCREEN_X, SCREEN_Y);
	}

	public void init(GLAutoDrawable drawable)
	{
		_gl = drawable.getGL();
		_gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//NyARToolkitの準備
		try {
			//キャプチャの準備
			_capture = new JmfCameraCapture(SCREEN_X, SCREEN_Y, 15f, JmfCameraCapture.PIXEL_FORMAT_RGB);
			_capture.setCaptureListener(this);
			//NyARToolkitの準備
			_ar_param = new GLNyARParam();
			_ar_param.loadARParamFromFile(PARAM_FILE);
			_ar_param.changeScreenSize(SCREEN_X, SCREEN_Y);

			//ARコードを2個ロード
			double[] width = new double[] { 80.0, 80.0 };
			NyARCode[] ar_codes = new NyARCode[2];
			ar_codes[0] = new NyARCode(16, 16);
			ar_codes[0].loadFromARFile(CARCODE_FILE1);
			ar_codes[1] = new NyARCode(16, 16);
			ar_codes[1].loadFromARFile(CARCODE_FILE2);
			_nya = new GLNyARDetectMarker(_ar_param, ar_codes, width, 2);
			_nya.setContinueMode(false);//ここをtrueにすると、transMatContinueモード（History計算）になります。
			//NyARToolkit用の支援クラス
			_glnya = new NyARGLUtil(_gl);
			//GL対応のRGBラスタオブジェクト
			_cap_image = new GLNyARRaster_RGB(_ar_param);
			//キャプチャ開始
			_capture.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
		_animator = new Animator(drawable);

		_animator.start();

	}

	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height)
	{
		_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
		_gl.glViewport(0, 0, width, height);

		//視体積の設定
		_gl.glMatrixMode(GL.GL_PROJECTION);
		_gl.glLoadIdentity();
		//見る位置
		_gl.glMatrixMode(GL.GL_MODELVIEW);
		_gl.glLoadIdentity();
	}

	public void display(GLAutoDrawable drawable)
	{

		try {
			if (!_cap_image.hasData()) {
				return;
			}
			_gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT); // Clear the buffers for new frame.          
			//画像チェックしてマーカー探して、背景を書く
			int found_markers;
			synchronized (_cap_image) {
				found_markers = _nya.detectMarkerLite(_cap_image, 110);
				//背景を書く
				_glnya.drawBackGround(_cap_image, 1.0);
			}
			//あったら立方体を書く
			double[] matrix = new double[16];
			for (int i = 0; i < found_markers; i++) {
				//1番のマーカーでなければ表示しない。
				if (_nya.getARCodeIndex(i) != 0) {
					continue;
				}
				//マーカーの一致度を調査するならば、ここでnya.getConfidence()で一致度を調べて下さい。
				// Projection transformation.
				_gl.glMatrixMode(GL.GL_PROJECTION);
				_gl.glLoadMatrixd(_ar_param.getCameraFrustumRH(), 0);
				_gl.glMatrixMode(GL.GL_MODELVIEW);
				// Viewing transformation.
				_gl.glLoadIdentity();
				_nya.getCameraViewRH(i, matrix);
				_gl.glLoadMatrixd(matrix, 0);

				// All other lighting and geometry goes here.
				drawCube();
			}
			Thread.sleep(1);//タスク実行権限を一旦渡す            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void onUpdateBuffer(Buffer i_buffer)
	{
		try {
			synchronized (_cap_image) {
				_cap_image.setBuffer(i_buffer, true);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void displayChanged(GLAutoDrawable drawable, boolean modeChanged, boolean deviceChanged)
	{
	}

	public static void main(String[] args)
	{
		new JavaSimpleLite2();
	}
}
