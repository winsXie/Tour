package com.bn.getnativepicture;

import java.io.Serializable;

/**
 * 单个图片信息类
 */
public class ImageItem implements Serializable
{
	private static final long serialVersionUID = 1L;
	public String imageId;//图片id
	public String thumbnailPath;//所在相册的缩略图路径
	public String imagePath;//图片原路径
	public boolean isSelected = false;//是否被选中
}
