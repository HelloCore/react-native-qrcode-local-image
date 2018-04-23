//
//  RCTQRCodeLocalImage.m
//  RCTQRCodeLocalImage
//
//  Created by fangyunjiang on 15/11/4.
//  Copyright (c) 2015年 remobile. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <React/RCTLog.h>
#import <React/RCTUtils.h>
#import "RCTQRCodeLocalImage.h"
#import <AssetsLibrary/AssetsLibrary.h>
#import <Photos/Photos.h>

@implementation RCTQRCodeLocalImage
RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(decode:(NSString *)path callback:(RCTResponseSenderBlock)callback)
{
	
	
	ALAssetsLibraryAssetForURLResultBlock resultblock = ^(ALAsset *myasset)
	{
		ALAssetRepresentation *rep = [myasset defaultRepresentation];
		float width = [rep dimensions].width;
		float height = [rep dimensions].height;
		//      CGImageRef iref = CGImageCreateWithImageInRect([rep fullResolutionImage], CGRectMake(width/2, 0, width/2, height));
		CGImageRef iref =  rep.fullScreenImage;
		if (nil==rep){
			NSLog(@"PROBLEM! IMAGE NOT LOADED\n");
			callback(@[RCTMakeError(@"IMAGE NOT LOADED!", nil, nil)]);
			return;
		}
		
		//         Write image to test
		
		//        NSArray *paths = NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES);
		//        NSString *fileName = [rep filename];
		//        NSString *combined = [NSString stringWithFormat:@"ScreenShot - %@.png", fileName];
		//        NSString *filePath = [[paths objectAtIndex:0] stringByAppendingPathComponent:combined];
		//
		//        UIImage *currentImage = [UIImage imageWithCGImage:iref];
		//        NSData *currentImageData = UIImagePNGRepresentation(currentImage);
		//        [currentImageData writeToFile:filePath atomically:YES];
		//
		NSLog(@"OK - IMAGE LOADED\n");
		NSDictionary *detectorOptions = @{@"CIDetectorAccuracy": @"CIDetectorAccuracyHigh"};
		CIDetector *detector = [CIDetector detectorOfType:CIDetectorTypeQRCode context:nil options:detectorOptions];
		CIImage *image = [CIImage imageWithCGImage:iref];
		NSArray *features = [detector featuresInImage:image];
		if (0==features.count) {
			NSLog(@"PROBLEM! Feature size is zero!\n");
			callback(@[RCTMakeError(@"Feature size is zero!", nil, nil)]);
			return;
		}
		
		CIQRCodeFeature *feature = [features firstObject];
		
		NSString *result = feature.messageString;
		NSLog(@"result: %@", result);
		
		if (result) {
			callback(@[[NSNull null], result]);
		} else {
			callback(@[RCTMakeError(@"QR Parse failed!", nil, nil)]);
			return;
		}
	};
	
	ALAssetsLibraryAccessFailureBlock failureblock  = ^(NSError *myerror){
		
		//failed to get image.
	};
	
	//    ALAssetsLibrary* assetslibrary = [[ALAssetsLibrary alloc] init];
	NSURL *myAssetUrl = [NSURL URLWithString:[path stringByAddingPercentEscapesUsingEncoding:NSUTF8StringEncoding]];
	//    [assetslibrary assetForURL:myAssetUrl resultBlock:resultblock failureBlock:failureblock];
	PHFetchResult<PHAsset *> *fetchResult = [PHAsset fetchAssetsWithALAssetURLs:@[myAssetUrl] options:0];
	
	PHAsset *asset = fetchResult.firstObject;
	PHImageRequestOptions *options = [[PHImageRequestOptions alloc] init];
	options.version = PHImageRequestOptionsVersionOriginal;
	options.deliveryMode = PHImageRequestOptionsDeliveryModeHighQualityFormat;
	options.resizeMode = PHImageRequestOptionsResizeModeNone;
	
	[[PHImageManager defaultManager] requestImageDataForAsset:asset options:options resultHandler:^(NSData * _Nullable imageData, NSString * _Nullable dataUTI, UIImageOrientation orientation, NSDictionary * _Nullable info) {
		//		UIImage* image = [[UIImage alloc] initWithData:imageData];
		
		//		float width = image.size.width;
		//		float height = image.size.height;
		//
		//		CGImageRef iref =  image.CGImage;
		
		NSLog(@"OK - IMAGE LOADED\n");
		NSDictionary *detectorOptions = @{@"CIDetectorAccuracy": @"CIDetectorAccuracyHigh"};
		CIDetector *detector = [CIDetector detectorOfType:CIDetectorTypeQRCode context:nil options:detectorOptions];
		
		NSArray *features = [detector featuresInImage: [[CIImage alloc] initWithData:imageData]];
		
		
		//		NSArray *features = [detector featuresInImage:image.CIImage];
		if (0==features.count) {
			NSLog(@"PROBLEM! Feature size is zero!\n");
			callback(@[RCTMakeError(@"Feature size is zero!", nil, nil)]);
			return;
		}
		
		CIQRCodeFeature *feature = [features firstObject];
		
		NSString *result = feature.messageString;
		NSLog(@"result: %@", result);
		
		if (result) {
			callback(@[[NSNull null], result]);
		} else {
			callback(@[RCTMakeError(@"QR Parse failed!", nil, nil)]);
			return;
		}
		
	}];
	
}

@end

