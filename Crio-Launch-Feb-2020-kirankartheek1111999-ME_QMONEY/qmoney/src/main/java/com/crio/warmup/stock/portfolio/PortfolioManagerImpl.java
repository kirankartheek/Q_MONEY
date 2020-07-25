package com.crio.warmup.stock.portfolio;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private RestTemplate restTemplate;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!



  // Caution: Do not delete or modify the constructor, or else your build will break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Now we want to convert our code into a module, so we will not call it from
  // main anymore.
  // Copy your code from Module#3
  // PortfolioManagerApplication#calculateAnnualizedReturn
  // into #calculateAnnualizedReturn function here and make sure that it
  // follows the method signature.
  // Logic to read Json file and convert them into Objects will not be required
  // further as our
  // clients will take care of it, going forward.
  // Test your code using Junits provided.
  // Make sure that all of the tests inside PortfolioManagerTest using command
  // below -
  // ./gradlew test --tests PortfolioManagerTest
  // This will guard you against any regressions.
  // run ./gradlew build in order to test yout code, and make sure that
  // the tests and static code quality pass.

  // CHECKSTYLE:OFF






  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo thirdparty APIs to a separate function.
  // It should be split into fto parts.
  // Part#1 - Prepare the Url to call Tiingo based on a template constant,
  // by replacing the placeholders.
  // Constant should look like
  // https://api.tiingo.com/tiingo/daily/<ticker>/prices?startDate=?&endDate=?&token=?
  // Where ? are replaced with something similar to <ticker> and then actual url
  // produced by
  // replacing the placeholders with actual parameters.

  public List<TiingoCandle> getStockQuote(String symbol, LocalDate from, LocalDate to) throws JsonProcessingException {
    String url = buildUri(symbol, from, to);
    RestTemplate restTemplate = this.restTemplate;
    TiingoCandle[] stockQuotes = restTemplate.getForObject(url, TiingoCandle[].class);
    /*
     * List<TiingoCandle> candles = new ArrayList<TiingoCandle>();
     * candles.add(stockQuotes[0]); candles.add(stockQuotes[stockQuotes.length-1]);
     */
    if (stockQuotes != null)
      return Arrays.asList(stockQuotes);
    else
      return Collections.emptyList();
  }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
        + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
    String token = "910fefa416692213ec155cccafe2c9c23ef63594";
    String url = uriTemplate.replace("$SYMBOL", symbol).replace("$STARTDATE", startDate.toString())
        .replace("$ENDDATE", endDate.toString()).replace("$APIKEY", token);
    return url;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate) {
    List<AnnualizedReturn> annualizedReturns = new ArrayList<AnnualizedReturn>();

    for (PortfolioTrade trade : portfolioTrades) {
      LocalDate startDate = trade.getPurchaseDate();
      List<TiingoCandle> candle = new ArrayList<TiingoCandle>();
      try {
        candle = getStockQuote(trade.symbol, startDate, endDate);
      } catch (JsonProcessingException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      Double buyPrice = candle.get(0).getOpen();
      Double sellPrice = candle.get(candle.size() - 1).getClose();
      Double totalReturns = (sellPrice - buyPrice) / buyPrice;
      Double totalnumyears = (endDate.getYear() - startDate.getYear()) / 1.00
          + (endDate.getMonthValue() - startDate.getMonthValue()) / 12.00
          + (endDate.getDayOfMonth() - startDate.getDayOfMonth()) / 365.00;
      Double annualizedreturn = StrictMath.pow(1 + totalReturns, 1 / totalnumyears) - 1;
      annualizedReturns.add(new AnnualizedReturn(trade.getSymbol(), annualizedreturn, totalReturns));
    }
    annualizedReturns.sort(getComparator());
    // System.out.println(annualizedReturns);
    // return annualizedReturns;

    // annualizedReturns.sort(getComparator());
    return annualizedReturns;
  }



}
