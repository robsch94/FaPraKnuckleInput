# FaPraKnuckleInput

Mobile devices have become by far the most important digital devices over the last century.
They are used every day, all the time by almost everybody.
Nevertheless, their interaction methods so far are limited to simple touch inputs on the screen and a few hardware buttons.
Researchers already have proposed numerous advanced interaction techniques for solving the restrictions while only using one raw touch position.
In this paper, we want to focus on interaction with the knuckle in addition to normal index finger input on the touchscreen.
We propose a method for distinguishing between finger and knuckle inputs using a deep neural network while only relying on images provided by the already available touch sensors.
In addition, we propose different gestures with both the finger and knuckle once again using only the touch images and a deep neural network.
This allows for a broader input modality on mobile devices with a touchscreen while no additional hardware is needed.

## Demo App

The Demo app is capable of distinguishing:
* finger/knuckle input via CNN
* gestures via combined CNN + LSTM
It is located under CapacitiveImageViewer/

To switch between the two modes:
* CNN: in FullscreenActivity.java line 87, set the COMBINED_MODE variable = false.
* LSTM + CNN: in FullscreenActivity.java line 87, set the COMBINED_MODE variable = true and adjust the WINDOW_SIZE to your model.

## Study App
The Study app was used for data collection.
The app itself is located under KnuckleFinger/, the GUI for the PC under WizardGUI/.
The following steps need to be executed:
* Change the PC_IP variable in TaskActivity.java
* Change the PHONE_IP in WizardGUI/Starter.py
* Run the KnuckleFinger app
* Run the WizardGUI/Starter.py file

## Jupyter
This section describes, which notebooks have to be run for the wanted networks.
All notebooks are located under Jupyter/

Baseline:
* Step_1AB_ReadData
* Step_2A__1_Baselinecreation

CNN for Finger/Knuckle Recognition:
* Step_1AB_ReadData
* Step_2A_PreprocessData
* Step_3A_ModelTraining-Tensorboard

Gestures First Approach with summed-up images:
* Step_1AB_ReadData
* Step_2C_PreprocessData_Gestures
* Step_3C_ModelTraining_Gestures

Gestures LSTM with interpolation
* Step_1AB_ReadData
* Step_2B_1_PreprocessData_Filtering
* Step_2B_2_PreprocessData_LSTM_Interpolation
* Step_3B_ModelTraining-LSTM-Interpolate

Gestures LSTM with cutting
* Step_1AB_ReadData
* Step_2B_1_PreprocessData_Filtering
* Step_2B_2_PreprocessData_LSTM_Cutting
    one of the following:
        * Step_3B_ModelTraining-LSTM-L1L2 (w/ regularization)
        * Step_3B_ModelTraining-LSTM-NoRegu (w/o regularization)