# FaPraKnuckleInput

This Document describes, which notebooks have to be run for the wanted networks.
All notebooks are located under Jupyter/

Baseline:
    Step_1AB_ReadData
    Step_2A__1_Baselinecreation

CNN for Finger/Knuckle Recognition:
    Step_1AB_ReadData
    Step_2A_PreprocessData
    Step_3A_ModelTraining-Tensorboard

Gestures First Approach with summed-up images:
    Step_1AB_ReadData
    Step_2C_PreprocessData_Gestures
    Step_3C_ModelTraining_Gestures

Gestures LSTM with interpolation
    Step_1AB_ReadData
    Step_2B_1_PreprocessData_Filtering
    Step_2B_2_PreprocessData_LSTM_Interpolation
    Step_3B_ModelTraining-LSTM-Interpolate

Gestures LSTM with cutting
    Step_1AB_ReadData
    Step_2B_1_PreprocessData_Filtering
    Step_2B_2_PreprocessData_LSTM_Cutting
    one of the following:
        Step_3B_ModelTraining-LSTM-L1L2 (w/ regularization)
        Step_3B_ModelTraining-LSTM-NoRegu (w/o regularization)