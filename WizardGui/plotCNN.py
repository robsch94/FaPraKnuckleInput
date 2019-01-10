import matplotlib.pyplot as plt
import csv

x1 = []
y1 = []
x2 = []
y2 = []

with open('C:/Users/Simon/Desktop/example.csv','r') as csvfile:
    plots = csv.reader(csvfile, delimiter=',')
    for row in plots:
        try:
            x1.append(int(row[0]))
            y1.append(int(row[1]))
        except:
            continue

with open('C:/Users/Simon/Desktop/example2.csv','r') as csvfile:
    plots = csv.reader(csvfile, delimiter=',')
    for row in plots:
        try:
            x2.append(int(row[0]))
            y2.append(int(row[1]))
        except:
            continue

plt.plot(x1,y1, label='Loaded from file!')
plt.plot(x2,y2, label='Loaded from file!')
plt.xlabel('x')
plt.ylabel('y')
plt.title('Interesting Graph\nCheck it out')
plt.legend()
plt.show()