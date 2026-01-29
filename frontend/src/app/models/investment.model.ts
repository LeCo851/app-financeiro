export interface Investment {
  id?: string;
  pluggyInvestmentId: string;
  name: string;
  code?: string; // Código do ativo (ex: PETR4)
  isin?: string;
  type: string;  // FIXED_INCOME, MUTUAL_FUND, etc.
  subType?: string;
  balance: number;       // Saldo Atual
  amountInvested?: number; // Valor Aplicado
  quantity?: number;
  annualRate?: number;
  rateType?: string; // CDI, SELIC, IPCA
  dueDate?: string;  // Data de vencimento
  status: string;
}
