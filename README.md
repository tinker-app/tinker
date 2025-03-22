# Tinker

| Home | Example usage |
|------|---------|
| <img src="https://github.com/tinker-app/tinker/blob/main/home.png?raw=true" width="80%"> |  <img src="https://github.com/tinker-app/tinker/blob/main/usage_gif.gif?raw=true" width="80%"> |

## Inspiration
Looking through Amazon Search queries can get boring. This app tries to make this process easier/more fun.

## What it does
This app allows users to doom-scroll through thousands of Amazon product listings in as a Tinder alternative for tech enthusiasts. Each left or right swipe takes into consideration user's preferences (Either by using the latent factors matrix given by SVD or by using incremental change formula for Stochastic gradient descent). As the user keeps scrolling, the results hone in on what the user might prefer according to the decisions made by the user(Kind of like Akinator).

## How we built it
The database is stored in Firestore and it contains information regarding about a thousand tablets, phones, and laptops. The app is built on Android as a proof of concept as the three of us are familar with  this stack. We used a Python Script utilizing Selenium and Chrome Webdriver to crawl through Amazon search results in order to collect the products and process them into Vectors. The app queries this pre-processed database and applies several Numerical algorithms with addition to user actions to predict the most accurate guesses for what a user prefers the most.

## Challenges we ran into
- Loading the data asynchronously onto a Mobile device is not easy as these devices are made for optimized battery consumption.
- HTTP requests for the Amazon products must be made carefully to avoid the engine from losing access to the URL or getting the IP banned
- The accuracy of the vector fields is paramount as the successful correlation between a User's preferences about Ram, Price, Display Size, and other fields heavily rely on the vectors being accurate representations of the Device specifications. This was tough since the data was not stored in a normalize way and it had to be queries using keywords. Hence, loading the data was the slowest part of the app-building process.
- Callback functions and Thread holds were required to stop the Android UI thread from breaking the app. This required some knowledge of Operating systems concepts that we learned on the fly.

## Accomplishments that we're proud of
- Successfuly loaded upto 1000+ Phones, Tablets, Laptops onto our database, processed and cleaned the data into vectors, and applied Numerical Methods onto them to decisively produce reliable guesses for User's preferences.

## What we learned
We learned different aspects of web scraping with Selenium, managing multiple threads while loading a large amount of data onto an Android app(Asynchronously), Using Singular Value Decomposition and Stchastic Gradient Descent for augmenting the accuracy of a feature vector, while providing the closet approximate guesses for the solution vector using Cosine Similarity.

## What's next for Tinker
Our goals include real-time updates, custom search queries for users, custom product categories and filters. We also hope to store a database of feature vectors for each user so that their preferences are retained.
