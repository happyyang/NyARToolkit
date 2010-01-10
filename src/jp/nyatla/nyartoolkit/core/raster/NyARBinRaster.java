/* 
 * PROJECT: NyARToolkit
 * --------------------------------------------------------------------------------
 * This work is based on the original ARToolKit developed by
 *   Hirokazu Kato
 *   Mark Billinghurst
 *   HITLab, University of Washington, Seattle
 * http://www.hitl.washington.edu/artoolkit/
 *
 * The NyARToolkit is Java edition ARToolKit class library.
 * Copyright (C)2008-2009 Ryo Iizuka
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * For further information please contact.
 *	http://nyatla.jp/nyatoolkit/
 *	<airmail(at)ebony.plala.or.jp> or <nyatla(at)nyatla.jp>
 * 
 */
package jp.nyatla.nyartoolkit.core.raster;

import jp.nyatla.nyartoolkit.core.rasterreader.*;
import jp.nyatla.nyartoolkit.core.types.*;
import jp.nyatla.nyartoolkit.*;

public class NyARBinRaster extends NyARRaster_BasicClass
{
	protected Object _buf;
	protected INyARBufferReader _buffer_reader;

	public NyARBinRaster(int i_width, int i_height,int i_raster_type) throws NyARException
	{
		super(new NyARIntSize(i_width,i_height));
		if(!initInstance(this._size,i_raster_type)){
			throw new NyARException();
		}
	}
	public NyARBinRaster(int i_width, int i_height) throws NyARException
	{
		super(new NyARIntSize(i_width,i_height));
		if(!initInstance(this._size,INyARBufferReader.BUFFERFORMAT_INT1D_BIN_8)){
			throw new NyARException();
		}
	}	
	public INyARBufferReader getBufferReader()
	{
		return this._buffer_reader;
	}
	protected boolean initInstance(NyARIntSize i_size,int i_buf_type)
	{
		switch(i_buf_type)
		{
			case INyARBufferReader.BUFFERFORMAT_INT1D_BIN_8:
				this._buf = new int[i_size.w*i_size.h];
				this._buffer_reader=new NyARBufferReader(this._buf,INyARBufferReader.BUFFERFORMAT_INT1D_BIN_8);
				break;
			default:
				return false;
		}
		this._buffer_reader=new NyARBufferReader(this._buf,i_buf_type);
		return true;
	}	
}
