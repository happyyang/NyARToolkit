/* 
 * PROJECT: NyARToolkit(Extension)
 * --------------------------------------------------------------------------------
 *
 * The NyARToolkit is Java version ARToolkit class library.
 * Copyright (C)2008 R.Iizuka
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this framework; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.transmat.solver;

import jp.nyatla.nyartoolkit.NyARException;
import jp.nyatla.nyartoolkit.core.param.NyARPerspectiveProjectionMatrix;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint3d;

/**
 * 並進ベクトル[T]を３次元座標[b]と基点の回転済行列[M]から計算します。
 * 
 * アルゴリズムは、ARToolKit 拡張現実プログラミング入門 の、P207のものです。
 * 
 * 計算手順
 * [A]*[T]=bを、[A]T*[A]*[T]=[A]T*[b]にする。
 * set2dVertexで[A]T*[A]=[M]を計算して、Aの3列目の情報だけ保存しておく。
 * getTransportVectorで[M]*[T]=[A]T*[b]を連立方程式で解いて、[T]を得る。
 */
public class NyARTransportVectorSolver implements INyARTransportVectorSolver
{
	private double[] _cx;
	private double[] _cy;	
	private final NyARPerspectiveProjectionMatrix _projection_mat;
	private int _nmber_of_vertex;
	public NyARTransportVectorSolver(final NyARPerspectiveProjectionMatrix i_projection_mat_ref,int i_max_vertex)
	{
		this._projection_mat=i_projection_mat_ref;
		this._cx=new double[i_max_vertex];
		this._cy=new double[i_max_vertex];	
		return;
	}
	private double _a00,_a01,_a02_20,_a10,_a11,_a12_21,_a22;
	/**
	 * 画面上の座標群を指定します。
	 * @param i_ref_vertex_2d
	 * 歪み矯正済の画面上の頂点座標群への参照値を指定します。
	 * @throws NyARException
	 * 
	 */
	public void set2dVertex(NyARDoublePoint2d[] i_ref_vertex_2d,int i_number_of_vertex) throws NyARException
	{
		//3x2nと2n*3の行列から、最小二乗法計算するために3x3マトリクスを作る。		
		//[A]*[A]T
		//行列[A]の3列目のキャッシュ
		final double[] cx=this._cx;
		final double[] cy=this._cy;
		
		double m22;
		double p00=this._projection_mat.m00;
		double p01=this._projection_mat.m01;
		double p11=this._projection_mat.m11;
		double p12=this._projection_mat.m12;
		double p02=this._projection_mat.m02;
		double w1,w2,w3,w4;
		this._a00=i_number_of_vertex*p00*p00;
		this._a01=i_number_of_vertex*p00*p01;
		this._a10=i_number_of_vertex*p00*p01;
		this._a11=i_number_of_vertex*(p01*p01+p11*p11);
		
		m22=0;
		w1=w2=0;
		for(int i=0;i<i_number_of_vertex;i++){
			//座標を保存しておく。
			w3=p02-(cx[i]=i_ref_vertex_2d[i].x);
			w4=p12-(cy[i]=i_ref_vertex_2d[i].y);
			w1+=w3;
			w2+=w4;
			m22+=w3*w3+w4*w4;
		}
		this._a02_20=w1*p00;
		this._a12_21=p01*w1+p11*w2;
		this._a22=m22;

		this._nmber_of_vertex=i_number_of_vertex;
		return;
	}
	
	/**
	 * 画面座標群と3次元座標群から、平行移動量を計算します。
	 * 2d座標系は、直前に実行したset2dVertexのものを使用します。
	 * @param i_vertex_2d
	 * 直前のset2dVertexコールで指定したものと同じものを指定してください。
	 * @param i_vertex3d
	 * 3次元空間の座標群を設定します。頂点の順番は、画面座標群と同じ順序で格納してください。
	 * @param o_transfer
	 * @throws NyARException
	 */
	final public void solveTransportVector(NyARDoublePoint3d[] i_vertex3d,NyARDoublePoint3d o_transfer) throws NyARException
	{
		final int number_of_vertex=this._nmber_of_vertex;
		final double p00=this._projection_mat.m00;
		final double p01=this._projection_mat.m01;
		final double p02=this._projection_mat.m02;
		final double p11=this._projection_mat.m11;
		final double p12=this._projection_mat.m12;
		//行列[A]の3列目のキャッシュ
		final double[] cx=this._cx;
		final double[] cy=this._cy;			
		
		//回転行列を元座標の頂点群に適応
		//[A]T*[b]を計算
		double bt1=0,bt2=0,bt3=0;
		for(int i=0;i<number_of_vertex;i++)
		{
			double w1=i_vertex3d[i].z*cx[i]-p00*i_vertex3d[i].x-p01*i_vertex3d[i].y-p02*i_vertex3d[i].z;
			double w2=i_vertex3d[i].z*cy[i]-p11*i_vertex3d[i].y-p12*i_vertex3d[i].z;
			bt1+=w1;
			bt2+=w2;
			bt3+=-cx[i]*w1-cy[i]*w2;
		}
		//([A]*T[A])*[T]=[A]T*[b]を方程式で解く。
		//a01とa10を0と仮定しても良いんじゃないかな？
		double b00=bt1*p00;
		double m00=this._a00;
		double m10=this._a10;
		double m20=this._a02_20;
		double m12_21=this._a12_21;
		double m01=this._a01;

		double a=(this._a11*m00-m10*m01);	
		double b=(m00*m12_21-m20*m01);	
		double c=(m00*this._a22-m20*m20);	
		double d=(m00*(bt1*p02+bt2*p12+bt3)-m20*b00);	

		double z,y;
		o_transfer.z=z=((a*d)-(b*(m00*(b00+bt2*p11)-m10*b00)))/((c*a)-(b*(m00*m12_21-m10)));
		o_transfer.y=y=(d-c*z)/(b);
		o_transfer.x=(b00-m01*y-m20*z)/m00;
		return;
	}
}