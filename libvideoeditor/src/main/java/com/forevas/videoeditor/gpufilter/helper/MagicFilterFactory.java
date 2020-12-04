package com.forevas.videoeditor.gpufilter.helper;

import com.forevas.videoeditor.gpufilter.basefilter.GPUImageFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicAntiqueFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicBrannanFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicCoolFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicFreudFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicHefeFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicHudsonFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicInkwellFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicN1977Filter;
import com.forevas.videoeditor.gpufilter.filters.MagicNashvilleFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicPixarFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicRomanceFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicSketchFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicSkinWhitenFilter;
import com.forevas.videoeditor.gpufilter.filters.MagicWarmFilter;
import com.forevas.videoeditor.gpufilter.gpuimage.GPUImageBrightnessFilter;
import com.forevas.videoeditor.gpufilter.gpuimage.GPUImageContrastFilter;
import com.forevas.videoeditor.gpufilter.gpuimage.GPUImageExposureFilter;
import com.forevas.videoeditor.gpufilter.gpuimage.GPUImageHueFilter;
import com.forevas.videoeditor.gpufilter.gpuimage.GPUImageSaturationFilter;
import com.forevas.videoeditor.gpufilter.gpuimage.GPUImageSharpenFilter;

public class MagicFilterFactory {
	
	private static MagicFilterType filterType = MagicFilterType.NONE;
	
	public static GPUImageFilter initFilters(MagicFilterType type){
		if(type==null){
			return null;
		}
		filterType = type;
		switch (type) {
		case SKINWHITEN:
			return new MagicSkinWhitenFilter();
		case ROMANCE:
			return new MagicRomanceFilter();
		case ANTIQUE:
			return new MagicAntiqueFilter();
		case BRANNAN:
			return new MagicBrannanFilter();
		case FREUD:
			return new MagicFreudFilter();
		case HEFE:
			return new MagicHefeFilter();
		case HUDSON:
			return new MagicHudsonFilter();
		case INKWELL:
			return new MagicInkwellFilter();
		case N1977:
			return new MagicN1977Filter();
		case NASHVILLE:
			return new MagicNashvilleFilter();
		case PIXAR:
			return new MagicPixarFilter();
		case COOL:
			return new MagicCoolFilter();
		case WARM:
			return new MagicWarmFilter();
		case SKETCH:
			return new MagicSketchFilter();
		//image adjust
		case BRIGHTNESS:
			return new GPUImageBrightnessFilter();
		case CONTRAST:
			return new GPUImageContrastFilter();
		case EXPOSURE:
			return new GPUImageExposureFilter();
		case HUE:
			return new GPUImageHueFilter();
		case SATURATION:
			return new GPUImageSaturationFilter();
		case SHARPEN:
			return new GPUImageSharpenFilter();
		default:
			return null;
		}
	}
	
	public MagicFilterType getCurrentFilterType(){
		return filterType;
	}
}
