/*
 * This file is part of bisq.
 *
 * bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bisq.core.trade.protocol.tasks.taker;

import com.google.common.util.concurrent.FutureCallback;
import io.bisq.common.taskrunner.TaskRunner;
import io.bisq.core.trade.Trade;
import io.bisq.core.trade.protocol.tasks.TradeTask;
import org.bitcoinj.core.Sha256Hash;
import lombok.extern.slf4j.Slf4j;
import org.bitcoinj.core.Transaction;
import org.jetbrains.annotations.NotNull;

@Slf4j
public class TakerPublishTakerFeeTx extends TradeTask {
    @SuppressWarnings({"WeakerAccess", "unused"})
    public TakerPublishTakerFeeTx(TaskRunner taskHandler, Trade trade) {
        super(taskHandler, trade);
    }

    @Override
    protected void run() {
        try {
            runInterceptHook();
            processModel.getTradeWalletService().broadcastTx(processModel.getTradeWalletService().getWalletTx(processModel.getTakeOfferFeeTx().getHash()),
                    new FutureCallback<Transaction>() {
                        @Override
                        public void onSuccess(Transaction transaction) {
                            log.debug("Trading fee published successfully. Transaction ID = " + transaction.getHashAsString());

                            trade.setState(Trade.State.TAKER_PUBLISHED_TAKER_FEE_TX);
                            complete();
                        }

                        @Override
                        public void onFailure(@NotNull Throwable t) {
                            appendToErrorMessage("Trading fee payment failed. Maybe your network connection was lost. Please try again.");
                            failed(t);
                        }
                    });
        } catch (Throwable t) {
            failed(t);
        }
    }
}
