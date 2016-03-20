package jp.nyatla.nyartoolkit.core.kpm.matcher.homography_estimation;

import jp.nyatla.nyartoolkit.core.kpm.matcher.HomographyMat;
import jp.nyatla.nyartoolkit.core.types.NyARDoublePoint2d;
import jp.nyatla.nyartoolkit.core.types.matrix.NyARDoubleMatrix33;

public class HomographyPointsCheck
{
    /**
     * Check the geometric consistency between three correspondences.
     */
    private static boolean Homography3PointsGeometricallyConsistent(NyARDoublePoint2d x1, NyARDoublePoint2d x2, NyARDoublePoint2d x3,NyARDoublePoint2d x1p, NyARDoublePoint2d x2p, NyARDoublePoint2d x3p)
    {
        if(((geometry.LinePointSide(x1, x2, x3) > 0) ^ (geometry.LinePointSide(x1p, x2p, x3p) > 0)) == true) {
            return false;
        }
        return true;
    }
    private double _w,_h;
    public void setTestWindow(double i_w,double i_h)
    {
    	this._w=i_w;
    	this._h=i_h;
    }
	/**
	 * 4ポイント限定のHomographyPointsGeometricallyConsistent関数
	 * @param H
	 * @param i_width
	 * @param i_height
	 * @return
	 */
    public boolean geometricallyConsistent(HomographyMat H)
    {
    	NyARDoublePoint2d[] x= NyARDoublePoint2d.createArray(4);
		x[0].x = 0;
		x[0].y = 0;
		x[1].x = this._w;
		x[1].y = 0;
		x[2].x = this._w;
		x[2].y = this._h;
		x[3].x = 0;
		x[3].y = this._h; 
    	NyARDoublePoint2d xp1=new NyARDoublePoint2d();
    	NyARDoublePoint2d xp2=new NyARDoublePoint2d();
    	NyARDoublePoint2d xp3=new NyARDoublePoint2d();
    	NyARDoublePoint2d first_xp1=new NyARDoublePoint2d();
    	NyARDoublePoint2d first_xp2=new NyARDoublePoint2d();
        
        
        int x1_ptr = 0;
//        int x2_ptr = 0+1;
//        int x3_ptr = 0+2;
        
        NyARDoublePoint2d xp1_ptr = xp1;
        NyARDoublePoint2d xp2_ptr = xp2;
        NyARDoublePoint2d xp3_ptr = xp3;
        
        //
        // Check the first 3 points
        //        
        MultiplyPointHomographyInhomogenous(xp1, H,x[x1_ptr]);
        MultiplyPointHomographyInhomogenous(xp2, H,x[x1_ptr+1]);
        MultiplyPointHomographyInhomogenous(xp3, H,x[x1_ptr+2]);
        
        first_xp1.setValue(xp1);//indexing.CopyVector2(first_xp1,0, xp1,0);
        first_xp2.setValue(xp2);//indexing.CopyVector2(first_xp2,0, xp2,0);
//    	public boolean Homography4PointsGeometricallyConsistent(float[] x1, float[] x2, float[] x3, float[] x4,float[] x1p,float[] x2p,float[] x3p,float[] x4p) {
        
        
        if(!Homography3PointsGeometricallyConsistent(
        		x[x1_ptr],x[x1_ptr+1],x[x1_ptr+2],
        		xp1_ptr, xp2_ptr, xp3_ptr)) {
            return false;
        }
        
        //
        // Check the remaining points
        //
        
        for(int i = 3; i < x.length; i++) {
            x1_ptr += 1;
            MultiplyPointHomographyInhomogenous(xp1_ptr, H, x[x1_ptr+2]);
            
            NyARDoublePoint2d tmp_ptr = xp1_ptr;
            xp1_ptr = xp2_ptr;
            xp2_ptr = xp3_ptr;
            xp3_ptr = tmp_ptr;

            if(!Homography3PointsGeometricallyConsistent(
            		x[x1_ptr],x[x1_ptr+1],x[x1_ptr+2],
            		xp1_ptr, xp2_ptr, xp3_ptr)) {
                return false;
            }
        }
        
        //
        // Check the last 3 points
        //
        
        if(!Homography3PointsGeometricallyConsistent(
        		x[x1_ptr+1],x[x1_ptr+2],x[0],
        		xp2_ptr, xp3_ptr, first_xp1)) {
            return false;
        }
        if(!Homography3PointsGeometricallyConsistent(
        		x[x1_ptr+2],x[0],x[2],
        		xp3_ptr, first_xp1, first_xp2)) {
            return false;
        }
        
        return true;
    }
    /**
     * Multiply an in-homogenous point by a similarity.
     */
    private static void MultiplyPointHomographyInhomogenous(NyARDoublePoint2d xp,NyARDoubleMatrix33 H, NyARDoublePoint2d x)
    {
    	double w = H.m20*x.x + H.m21*x.y + H.m22;
        xp.x = (H.m00*x.x + H.m01*x.y + H.m02)/w;
        xp.y = (H.m10*x.x + H.m11*x.y + H.m12)/w;
    }    
}