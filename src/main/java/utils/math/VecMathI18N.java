/*
 * $RCSfile: VecMathI18N.java,v $
 *
 * Copyright (c) 2007 Sun Microsystems, Inc. All rights reserved.
 *
 * Use is subject to license terms.
 *
 * $Revision: 1.4 $
 * $Date: 2007/02/09 17:22:39 $
 * $State: Exp $
 */

package utils.math;



class VecMathI18N {
	static final String[] keys = {
		"Matrix3d0",
		"Matrix3d1",
		"Matrix3d2",
		"Matrix3d4",
		"Matrix3d6",
		"Matrix3d9",
		"Matrix3d12",
		"Matrix3d13",
		"Matrix3f0",
		"Matrix3f1",
		"Matrix3f3",
		"Matrix3f5",
		"Matrix3f6",
		"Matrix3f9",
		"Matrix3f12",
		"Matrix3f13",
		"Matrix4d0",
		"Matrix4d1",
		"Matrix4d2",
		"Matrix4d3",
		"Matrix4d4",
		"Matrix4d7",
		"Matrix4d10",
		"Matrix4d11",
		"Matrix4f0",
		"Matrix4f1",
		"Matrix4f2",
		"Matrix4f4",
		"Matrix4f6",
		"Matrix4f9",
		"Matrix4f12",
		"Matrix4f13",
		"GMatrix0",
		"GMatrix1",
		"GMatrix2",
		"GMatrix3",
		"GMatrix4",
		"GMatrix5",
		"GMatrix6",
		"GMatrix7",
		"GMatrix8",
		"GMatrix9",
		"GMatrix10",
		"GMatrix11",
		"GMatrix12",
		"GMatrix13", 
		"GMatrix14",
		"GMatrix15",
		"GMatrix16",
		"GMatrix17",
		"GMatrix18",
		"GMatrix19",
		"GMatrix20",
		"GMatrix21",
		"GMatrix22",
		"GMatrix24",
		"GMatrix25",
		"GMatrix26",
		"GMatrix27",
		"GMatrix28",
		"GVector0",
		"GVector1",
		"GVector2",
		"GVector3",
		"GVector4",
		"GVector5",
		"GVector6",
		"GVector7",
		"GVector8",
		"GVector9",
		"GVector10",
		"GVector11",
		"GVector12",
		"GVector13",
		"GVector14",
		"GVector15",
		"GVector16",
		"GVector17",
		"GVector18",
		"GVector19",
		"GVector20",
		"GVector21",
		"GVector22",
		"GVector23",
		"GVector24",
		"GVector25"
	};
	
	static final String[] values = {
		"Matrix3d setElement",
		"Matrix3d getElement",
		"Matrix3d getRow",
		"Matrix3d getColumn",
		"Matrix3d setRow",
		"Matrix3d setColumn",
		"cannot invert matrix",
		"Logic error: imax < 0",
		"Matrix3f setElement",
		"Matrix3d getRow",
		"Matrix3d getColumn",
		"Matrix3f getElement",
		"Matrix3f setRow",
		"Matrix3f setColumn",
		"cannot invert matrix",
		"Logic error: imax < 0",
		"Matrix4d setElement",
		"Matrix4d getElement",
		"Matrix4d getRow",
		"Matrix4d getColumn",
		"Matrix4d setRow",
		"Matrix4d setColumn",
		"cannot invert matrix",
		"Logic error: imax < 0",
		"Matrix4f setElement",
		"Matrix4f getElement",
		"Matrix4f getRow",
		"Matrix4f getColumn",
		"Matrix4f setRow",
		"Matrix4f setColumn",
		"cannot invert matrix",
		"Logic error: imax < 0",
		"GMatrix.mul:array dimension mismatch ",
		"GMatrix.mul(GMatrix, GMatrix) dimension mismatch ",
		"GMatrix.mul(GVector, GVector): matrix does not have enough rows ",
		"GMatrix.mul(GVector, GVector): matrix does not have enough columns ",
		"GMatrix.add(GMatrix): row dimension mismatch ",
		"GMatrix.add(GMatrix): column dimension mismatch ",
		"GMatrix.add(GMatrix, GMatrix): row dimension mismatch ",
		"GMatrix.add(GMatrix, GMatrix): column dimension mismatch ",
		"GMatrix.add(GMatrix): input matrices dimensions do not match this matrix dimensions",
		"GMatrix.sub(GMatrix): row dimension mismatch ",
		"GMatrix.sub(GMatrix, GMatrix): row dimension mismatch ",
		"GMatrix.sub(GMatrix, GMatrix): column dimension mismatch ",
		"GMatrix.sub(GMatrix, GMatrix): input matrix dimensions do not match dimensions for this matrix ",
		"GMatrix.negate(GMatrix, GMatrix): input matrix dimensions do not match dimensions for this matrix ",
		"GMatrix.mulTransposeBoth matrix dimension mismatch",
		"GMatrix.mulTransposeRight matrix dimension mismatch",
		"GMatrix.mulTransposeLeft matrix dimension mismatch",
		"GMatrix.transpose(GMatrix) mismatch in matrix dimensions",
		"GMatrix.SVD: dimension mismatch with V matrix",
		"cannot perform LU decomposition on a non square matrix",
		"row permutation must be same dimension as matrix",
		"cannot invert matrix",
		"cannot invert non square matrix",
		"Logic error: imax < 0",
		"GMatrix.SVD: dimension mismatch with U matrix",
		"GMatrix.SVD: dimension mismatch with W matrix",
		"LU must have same dimensions as this matrix",
		"GMatrix.sub(GMatrix): column dimension mismatch",
		"GVector.normalize( GVector) input vector and this vector lengths not matched",
		"GVector.scale(double,  GVector) input vector and this vector lengths not matched",
		"GVector.scaleAdd(GVector, GVector) input vector dimensions not matched",
		"GVector.scaleAdd(GVector, GVector) input vectors and  this vector dimensions not matched",
		"GVector.add(GVector) input vectors and  this vector dimensions not matched",
		"GVector.add(GVector, GVector) input vector dimensions not matched",
		"GVector.add(GVector, GVector) input vectors and  this vector dimensions not matched",
		"GVector.sub(GVector) input vector and  this vector dimensions not matched",
		"GVector.sub(GVector,  GVector) input vector dimensions not matched",
		"GVector.sub(GMatrix,  GVector) input vectors and this vector dimensions not matched",
		"GVector.mul(GMatrix,  GVector) matrix and vector dimensions not matched",
		"GVector.mul(GMatrix,  GVector) matrix this vector dimensions not matched",
		"GVector.mul(GVector, GMatrix) matrix and vector dimensions not matched",
		"GVector.mul(GVector, GMatrix) matrix this vector dimensions not matched",
		"GVector.dot(GVector) input vector and this vector have different sizes",
		"matrix dimensions are not compatible ",
		"b vector does not match matrix dimension ",
		"GVector.interpolate(GVector, GVector, float) input vectors have different lengths ",
		"GVector.interpolate(GVector, GVector, float) input vectors and this vector have different lengths",
		"GVector.interpolate(GVector, float) input vector and this vector have different lengths",
		"GVector.interpolate(GVector, GVector, double) input vectors have different lengths ",
		"GVector.interpolate(GVector, GVector, double) input vectors and this vector have different lengths",
		"GVector.interpolate(GVector,  double) input vectors and this vector have different lengths",
		"matrix dimensions are not compatible",
		"permutation vector does not match matrix dimension",
		"LUDBackSolve non square matrix"
	};
	
    static String getString(String key) 
    {
    	String s = null;
    	for (int i=0; i<keys.length; i++)
    	{
    		String k = keys[i];
    		if (k.equalsIgnoreCase(key))
    		{
    			s = values[i];
    			break;
    		}
    	}
    	return s;
    }
}
