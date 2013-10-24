require(plotrix)

#
# maybe needed require(RCurl)
#temporaryFile <- tempfile()
#download.file("http://localhost:4000/trilaterate.csv",destfile=temporaryFile, method="curl")
#read.csv(temporaryFile)

# sample data set
#            x         y   radius      error
# 1 -1.9137300 -0.799904 2.040010  0.2402898
# 2 -0.9354530 -0.493735 0.959304  1.0866445
# 3  0.6309640 -0.653075 0.728477  1.8843792
# 4  0.3108570 -0.018258 0.301885 10.9727866
# 5  0.0431084  1.253210 1.190120  0.7060224

# sample <- read.csv("sample.csv")

errors <- 1/sample$radius^2

fit <- nls(radius ~ sqrt(abs((x-x0))^2+abs((y-y0))^2), data = sample, 
           start=list(x0=sample[1,]$x, y0=sample[1,]$y), weights = error) 

solution <- summary(fit)$coefficients[,1]

plot(sample$x, sample$y)
for(i in 1:nrow(sample)) {
  row <- sample[i,]
  draw.circle(row$x, row$y, row$radius)
}
draw.circle(solution["x0"], solution["y0"], 0.02,col="red")
rm(i)
rm(row)
print(summary(fit))