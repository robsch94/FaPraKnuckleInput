App is capable of distinguishing:
 - finger/knuckle input via CNN
 - gestures via combined CNN + LSTM

To switch between the two modes:
 - CNN: in FullscreenActivity.java line 87, set the COMBINED_MODE variable = false.
 - LSTM + CNN: in FullscreenActivity.java line 87, set the COMBINED_MODE variable = true and adjust the WINDOW_SIZE to your model.