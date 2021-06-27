/**
 * 
 */
package com.awsdemo.awspollyexamplespring;

import java.io.IOException;
import java.io.InputStream;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletResponse;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.polly.AmazonPolly;
import com.amazonaws.services.polly.AmazonPollyClient;
import com.amazonaws.services.polly.AmazonPollyClientBuilder;
import com.amazonaws.services.polly.model.DescribeVoicesRequest;
import com.amazonaws.services.polly.model.DescribeVoicesResult;
import com.amazonaws.services.polly.model.Gender;
import com.amazonaws.services.polly.model.OutputFormat;
import com.amazonaws.services.polly.model.SynthesizeSpeechRequest;
import com.amazonaws.services.polly.model.SynthesizeSpeechResult;
import com.amazonaws.services.polly.model.Voice;

import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

/**
 * @author mshaikh4
 *
 */
@Controller
public class PollyController {
	
	private AmazonPolly polly;
	private Voice voice;
	private static final String SAMPLE = "Congratulations. You have successfully built this working demo of Amazon Polly in Java. Have fun building voice enabled apps with Amazon Polly (that's me!), and always look at the AWS website for tips and tricks on using Amazon Polly and other great services from AWS";

	
	@PostConstruct
	void init() {
		// create an Amazon Polly client in a specific region
		polly = AmazonPollyClientBuilder.standard()
				.withCredentials(new DefaultAWSCredentialsProviderChain())
				.withClientConfiguration(new ClientConfiguration())
				.withRegion(Regions.AP_SOUTHEAST_1).build();
		
		// Create describe voices request.
		DescribeVoicesRequest describeVoicesRequest = new DescribeVoicesRequest();
		// Synchronously ask Amazon Polly to describe available TTS voices.
		DescribeVoicesResult describeVoicesResult = polly.describeVoices(describeVoicesRequest);
		System.out.println(describeVoicesResult.getVoices());
		voice = describeVoicesResult.getVoices().get(0);
		voice.setId("Salli");
		voice.setGender(Gender.Female);
	}
	
	public InputStream synthesize(String text, OutputFormat format) throws IOException {
		SynthesizeSpeechRequest synthReq = 
		new SynthesizeSpeechRequest().withText(text).withVoiceId(voice.getId())
				.withOutputFormat(format);
		SynthesizeSpeechResult synthRes = polly.synthesizeSpeech(synthReq);
		return synthRes.getAudioStream();
	}
	
	@GetMapping("/tts")
	public void getTTSFromAWS(HttpServletResponse response) {
		//get the audio stream
		InputStream speechStream = null;
				try {
					speechStream = synthesize(SAMPLE, OutputFormat.Mp3);
					
					/**
					//create an MP3 player
					AdvancedPlayer player = new AdvancedPlayer(speechStream,
							javazoom.jl.player.FactoryRegistry.systemRegistry().createAudioDevice());
					
					player.setPlayBackListener(new PlaybackListener() {
						@Override
						public void playbackStarted(PlaybackEvent evt) {
							System.out.println("Playback started");
							System.out.println(SAMPLE);
						}
						
						@Override
						public void playbackFinished(PlaybackEvent evt) {
							System.out.println("Playback finished");
						}
					});
					
					
					// play it!
					player.play();
					**/
					//int i = StreamUtils.copy(speechStream, response.getOutputStream());
					//response.setContentType("audio/mp3");
					//byte[] media = 
					/*
					 * byte[] buf = new byte[8192]; IOUtils.readFully(speechStream, buf);
					 * HttpHeaders headers = new HttpHeaders();
					 * headers.setCacheControl(CacheControl.noCache().getHeaderValue());
					 * ResponseEntity<byte[]> responseEntity = new ResponseEntity<>(buf, headers,
					 * HttpStatus.OK);
					 */
					//return responseEntity;
					//System.out.println(i);
					
					 response.setContentType("audio/mp3");
				        response.setHeader(
				                "Content-Disposition",
				                "attachment;filename=sample.mp3");
				        StreamUtils.copy(speechStream, response.getOutputStream());
				        response.flushBuffer();
				        
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	}
}
