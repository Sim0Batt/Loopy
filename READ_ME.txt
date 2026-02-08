\documentclass[11pt,a4paper]{article}

% --- Lingua e Codifica ---
\usepackage[utf8]{inputenc}
\usepackage[T1]{fontenc}
\usepackage{lmodern}

% --- Layout e Spaziatura ---
\usepackage{geometry}
\geometry{margin=2.5cm}

% Questo pacchetto è la chiave per risolvere il tuo problema visivo.
% Elimina il rientro della prima riga e aggiunge spazio tra i paragrafi.
\usepackage[skip=10pt plus1pt, indent=0pt]{parskip}

% Interlinea leggermente aumentata per leggibilità (meglio di onehalfspacing standard)
\linespread{1.15}

% --- Liste ---
\usepackage{enumitem}
% Configurazione globale liste: più spazio attorno, meno tra gli item
\setlist[itemize]{topsep=5pt, itemsep=2pt, parsep=2pt, leftmargin=*}
\setlist[enumerate]{topsep=5pt, itemsep=2pt, parsep=2pt, leftmargin=*}

% --- Header/Footer e Link ---
\usepackage{fancyhdr}
\usepackage[hidelinks]{hyperref} % Link cliccabili ma senza box colorati brutti

% --- Configurazione Header ---
\pagestyle{fancy}
\fancyhf{}
\lhead{\small \textbf{Loopy} -- Distributed Health Monitoring System}
\rhead{\small \nouppercase{\leftmark}}
\cfoot{\thepage}
\renewcommand{\headrulewidth}{0.4pt}

% --- Dati Titolo ---
\title{\textbf{\Huge Loopy}\\[0.5em] \Large Distributed Health Monitoring System}
\author{}
\date{}

\begin{document}

\maketitle
\thispagestyle{empty}

% Abstract o intro opzionale qui se serve

\newpage

\tableofcontents
\newpage

% =========================
\section{Project Overview}

\textbf{Loopy} is a distributed health monitoring system based on a wearable armband device. It is designed to continuously collect, process, and visualize physiological and activity-related data.

The system combines several key components:
\begin{itemize}
    \item an embedded device;
    \item a mobile Android application;
    \item multiple backend application servers;
    \item data analysis modules, including machine learning and artificial intelligence components.
\end{itemize}

The wearable device acquires raw sensor data such as heart rate, movement, body temperature, sweating, blood oxygenation, and other physiological signals.

These data are transmitted to backend servers for storage, processing, and analysis. Once processed, the insights are made available to the user directly through the mobile application.

Compared to a traditional smartwatch, Loopy aims to provide significant advantages:
\begin{itemize}
    \item improved comfort during long-term usage;
    \item higher-quality and more reliable health data;
    \item advanced data processing through machine learning algorithms;
    \item an AI-powered chatbot that offers personalized insights and lifestyle suggestions.
\end{itemize}

The mobile application acts as a remote control and visualization interface for the system. It allows users to:
\begin{itemize}
    \item monitor their health data in real time;
    \item check device status;
    \item interact with the AI assistant;
    \item manage their personal profile.
\end{itemize}

The overall architecture is modular and distributed, with each component designed to handle a specific responsibility within the system to ensure efficiency and scalability.

\newpage
% =========================
\section{System Architecture}

Loopy is designed as a \textbf{modular and distributed system}, where each component is responsible for a specific task within the overall health monitoring pipeline.

At a high level, the architecture is composed of four main layers:
\begin{itemize}
    \item a \textbf{wearable embedded device} based on a Raspberry Pi;
    \item a \textbf{mobile Android application};
    \item \textbf{cloud-based backend application servers};
    \item \textbf{data processing and analysis modules}, including machine learning components.
\end{itemize}

\subsection{Embedded Layer (Raspberry Pi)}

The wearable device hosts a Raspberry Pi that performs two main roles:
\begin{itemize}
    \item low-level interaction with physical sensors through Python-based scripts;
    \item execution of a JVM-based embedded application server responsible for coordinating data collection and communication with the backend.
\end{itemize}

Physiological and activity-related data (e.g., heart rate, movement, temperature, blood oxygenation) are acquired locally and prepared for transmission to the backend infrastructure.

\subsection{Backend Layer}

The backend is composed of two distinct application servers deployed on cloud infrastructure.

\subsubsection*{Application Server 1}
This server manages user authentication, device status, data storage, and communication with the mobile application.

\subsubsection*{Application Server 2}
This server is dedicated to data analysis and advanced processing tasks, including machine learning models, data visualization, and the AI-powered chatbot.

The separation of responsibilities between the two servers improves scalability, maintainability, and system robustness.

\subsection{Mobile Application Layer}

The Android mobile application acts as the main user interface of the system. It communicates with the backend servers to retrieve processed data, visualize health metrics, interact with the AI assistant, and monitor the status of the wearable device.

\subsection{Data Flow Overview}

\begin{enumerate}
    \item Sensor data are collected by the wearable device.
    \item Data are transmitted to the backend application servers.
    \item Backend servers store, process, and analyze the data.
    \item Processed information is sent to the mobile application for visualization and user interaction.
\end{enumerate}

\newpage
% =========================
\section{Project Layout}

The repository is organized in a modular way, reflecting the distributed architecture of the Loopy system. Each main directory corresponds to a specific component of the overall project.

\begin{verbatim}
Loopy/
├── mobile-app/
├── application-server-1/
├── application-server-2/
├── raspberry-pi-server-logic/
│   ├── main-server/
│   └── raspberry-sensors-logic/
├── metric-calculator/
├── glucose-calculator-ML/
└── documentation/
\end{verbatim}

\subsection*{Directory Description}

\begin{itemize}
    \item \textbf{mobile-app/} \\
    Contains the Android application developed in Kotlin, which provides the user interface for monitoring health data, managing the device, and interacting with the AI assistant.

    \item \textbf{application-server-1/} \\
    Backend application server responsible for user authentication, device management, data storage, and communication with the mobile application.

    \item \textbf{application-server-2/} \\
    Backend application server dedicated to data processing, machine learning tasks, graphical data analysis, and the AI-powered chatbot.

    \item \textbf{raspberry-pi-server-logic/} \\
    Contains all software components running on the wearable device:
    \begin{itemize}
        \item a JVM-based embedded application server (\texttt{main-server});
        \item Python scripts for direct interaction with hardware sensors (\texttt{raspberry-sensors-logic}).
    \end{itemize}

    \item \textbf{metric-calculator/} \\
    Python-based module used to compute daily and nightly health metrics from collected sensor data.

    \item \textbf{glucose-calculator-ML/} \\
    Machine learning module implementing data preprocessing, model training, and glucose level prediction.

    \item \textbf{documentation/} \\
    Contains project documentation, slides, and additional descriptive materials.
\end{itemize}

\newpage
% =========================
\section{Hardware and Software Requirements}

This section summarizes the hardware and software requirements needed to build and run the different components of the Loopy system. Due to the modular architecture of the project, not all components are required to be executed simultaneously.

\subsection{Hardware Requirements}
\begin{itemize}
    \item \textbf{Raspberry Pi} (used as the wearable embedded device)
    \item Health monitoring sensors (e.g., heart rate, temperature, motion, blood oxygenation)
    \item Android smartphone
    \item Cloud infrastructure (e.g., AWS EC2 or equivalent) for backend servers
\end{itemize}

\subsection{Software Requirements}
\begin{itemize}
    \item \textbf{Android Studio} (latest stable version) \\
    Required to build and run the mobile Android application.
    \item \textbf{Java Development Kit (JDK)} \\
    Required for all JVM-based components (backend servers and embedded server).
    \item \textbf{Python 3} \\
    Required for sensor logic, metric computation, and machine learning modules.
    \item \textbf{Gradle} \\
    Used as the build system for all JVM-based components (included via Gradle Wrapper).
    \item \textbf{Git} \\
    Used for version control and repository management.
\end{itemize}

Additional Python libraries and dependencies may be required for specific modules and are managed within the corresponding project directories.

\newpage
% =========================
\section{How to Build and Run the Project}

This section provides a high-level overview of how to build and run the main components of the Loopy system. Due to the modular nature of the project, components can be built and executed independently depending on the use case.

\subsection{Mobile Application}
The Android mobile application can be built and executed by opening the \texttt{mobile-app} directory with \textbf{Android Studio}. Once the project is loaded, the application can be run on an Android device or emulator using the standard Android Studio tools.

\subsection{Backend Application Servers}
Both backend servers (\texttt{application-server-1} and \texttt{application-server-2}) are JVM-based projects built using \textbf{Gradle}.

Each server can be built using the provided Gradle Wrapper and executed as a standalone application. The servers are intended to be deployed on cloud infrastructure (e.g., AWS EC2), but they can also be run locally for testing purposes.

\subsection{Embedded Raspberry Pi Components}
The Raspberry Pi hosts:
\begin{itemize}
    \item a JVM-based embedded application server;
    \item Python scripts for direct sensor interaction.
\end{itemize}

The embedded server is built using Gradle, while the sensor logic scripts are executed using Python. These components are intended to run directly on the Raspberry Pi device.

\subsection{Data Processing and Machine Learning Modules}
The metric computation and machine learning modules (\texttt{metric-calculator} and \texttt{glucose-calculator-ML}) are Python-based and can be executed independently to perform data analysis, model training, and prediction tasks.

Not all components are required to be running simultaneously to demonstrate or test individual functionalities of the system.

\newpage
% =========================
\section{User Guide}

The Loopy system is designed to be simple and intuitive for the end user. Interaction with the system mainly occurs through the Android mobile application, which acts as a remote control and visualization interface for the wearable device.

\subsection{Account Management}
To use the Loopy system, users must create an account through the mobile application by providing basic personal information. Once registered, users can log in, manage their profile, and delete their account at any time from the application settings.

\subsection{Application Usage}

\begin{itemize}
    \item \textbf{Home Page} \\
    Displays an overview of the user’s health status, including summaries of activity, sleep, stress levels, and recent heart rate data.

    \item \textbf{Data Page} \\
    Provides a detailed and numerical view of the health data collected during daily activities.

    \item \textbf{Chat Page} \\
    Allows users to interact with the AI-powered assistant, which provides personalized insights, summaries, and lifestyle suggestions based on collected data.

    \item \textbf{Device Manager} \\
    Displays the status of the wearable device and its sensors, helping users detect possible errors or malfunctions.

    \item \textbf{Profile Page} \\
    Enables users to view and manage personal information provided during registration.
\end{itemize}

The application continuously receives processed data from the backend servers and presents it in a clear and user-friendly way.

\newpage
% =========================
\section{External Resources}

Additional resources related to the Loopy project are provided below:

\begin{itemize}
    \item \textbf{Project Repository} \\
    The complete source code of the project is available in this GitHub repository.
    \item \textbf{Presentation Slides} \\
    A detailed presentation describing the project goals, architecture, and results is available in the \texttt{documentation} folder.
    \item \textbf{Demo Video} \\
    A demonstration video showing the Loopy system in action is available on YouTube. \\
    (Link to be added)
\end{itemize}

\newpage
% =========================
\section{Team Members and Contributions}

The Loopy project was developed as a collaborative effort by the following team members:

\begin{itemize}
    \item \textbf{Nicola Avellino} \\
    Contributed to the design and development of backend components, data management logic, and Raspberry Pi components.

    \item \textbf{Simone Battisti} \\
    Worked on backend services, data processing modules, artificial intelligence, and integration between system components.

    \item \textbf{Liam Dematt\`e} \\
    Contributed to the embedded system logic, sensor integration, and system architecture.

    \item \textbf{Riccardo Gonzato} \\
    Worked on the Android mobile application and contributed to system integration and documentation.
\end{itemize}

All team members collaborated on system design decisions, testing, and overall project integration.

\end{document}