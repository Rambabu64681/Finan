package com.Ram.financeapi;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/*
 * Finance Client Portfolio API
 * -----------------------------------------------------
 * Complete single-file Spring Boot project example.
 *
 * Features:
 * - Client management
 * - Portfolio creation
 * - BUY / SELL trade execution
 * - Holding updates
 * - Cash balance tracking
 * - Trade history
 * - Audit logs
 * - DTO validation
 * - Global exception handling
 *
 * Suggested file path:
 * src/main/java/com/amrutha/financeapi/FinanceClientPortfolioApiApplication.java
 */

@SpringBootApplication
public class FinanceClientPortfolioApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinanceClientPortfolioApiApplication.class, args);
    }

    @Bean
    CommandLineRunner seedData(ClientService clientService,
                               PortfolioService portfolioService,
                               TradeService tradeService) {
        return args -> {
            ClientResponse client = clientService.createClient(
                    new CreateClientRequest("Demo Client", "demo.client@example.com", RiskProfile.MODERATE)
            );

            PortfolioResponse portfolio = portfolioService.createPortfolio(
                    new CreatePortfolioRequest(client.id(), "Demo Growth Portfolio", new BigDecimal("50000"))
            );

            tradeService.executeTrade(
                    new TradeRequest(portfolio.id(), "AAPL", TradeType.BUY, 20, new BigDecimal("180.00"))
            );

            tradeService.executeTrade(
                    new TradeRequest(portfolio.id(), "MSFT", TradeType.BUY, 10, new BigDecimal("410.00"))
            );
        };
    }
}

/* =====================================================
   ENUMS
   ===================================================== */

enum RiskProfile {
    CONSERVATIVE,
    MODERATE,
    AGGRESSIVE
}

enum TradeType {
    BUY,
    SELL
}

/* =====================================================
   ENTITIES
   ===================================================== */

@Entity
@Table(name = "clients")
class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    private RiskProfile riskProfile;

    private LocalDateTime createdAt;

    protected Client() {
    }

    public Client(String fullName, String email, RiskProfile riskProfile) {
        this.fullName = fullName;
        this.email = email;
        this.riskProfile = riskProfile;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public RiskProfile getRiskProfile() {
        return riskProfile;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

@Entity
@Table(name = "portfolios")
class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String portfolioName;

    private BigDecimal cashBalance;

    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private Client client;

    protected Portfolio() {
    }

    public Portfolio(String portfolioName, BigDecimal cashBalance, Client client) {
        this.portfolioName = portfolioName;
        this.cashBalance = cashBalance;
        this.client = client;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getPortfolioName() {
        return portfolioName;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public void setCashBalance(BigDecimal cashBalance) {
        this.cashBalance = cashBalance;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Client getClient() {
        return client;
    }
}

@Entity
@Table(
        name = "holdings",
        uniqueConstraints = @UniqueConstraint(columnNames = {"portfolio_id", "symbol"})
)
class Holding {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    private Integer quantity;

    private BigDecimal averageCost;

    private LocalDateTime updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    protected Holding() {
    }

    public Holding(String symbol, Integer quantity, BigDecimal averageCost, Portfolio portfolio) {
        this.symbol = symbol;
        this.quantity = quantity;
        this.averageCost = averageCost;
        this.portfolio = portfolio;
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        this.updatedAt = LocalDateTime.now();
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
        this.updatedAt = LocalDateTime.now();
    }
}

@Entity
@Table(name = "trades")
class Trade {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String symbol;

    @Enumerated(EnumType.STRING)
    private TradeType tradeType;

    private Integer quantity;

    private BigDecimal price;

    private BigDecimal totalAmount;

    private LocalDateTime executedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id")
    private Portfolio portfolio;

    protected Trade() {
    }

    public Trade(String symbol,
                 TradeType tradeType,
                 Integer quantity,
                 BigDecimal price,
                 BigDecimal totalAmount,
                 Portfolio portfolio) {
        this.symbol = symbol;
        this.tradeType = tradeType;
        this.quantity = quantity;
        this.price = price;
        this.totalAmount = totalAmount;
        this.portfolio = portfolio;
        this.executedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getSymbol() {
        return symbol;
    }

    public TradeType getTradeType() {
        return tradeType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public LocalDateTime getExecutedAt() {
        return executedAt;
    }

    public Portfolio getPortfolio() {
        return portfolio;
    }
}

@Entity
@Table(name = "audit_logs")
class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String action;

    private String entityType;

    private Long entityId;

    private String description;

    private LocalDateTime createdAt;

    protected AuditLog() {
    }

    public AuditLog(String action, String entityType, Long entityId, String description) {
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.description = description;
        this.createdAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public Long getEntityId() {
        return entityId;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}

/* =====================================================
   DTOs
   ===================================================== */

record CreateClientRequest(
        @NotBlank(message = "Full name is required")
        String fullName,

        @Email(message = "Valid email is required")
        @NotBlank(message = "Email is required")
        String email,

        @NotNull(message = "Risk profile is required")
        RiskProfile riskProfile
) {
}

record ClientResponse(
        Long id,
        String fullName,
        String email,
        RiskProfile riskProfile,
        LocalDateTime createdAt
) {
}

record CreatePortfolioRequest(
        @NotNull(message = "Client id is required")
        Long clientId,

        @NotBlank(message = "Portfolio name is required")
        String portfolioName,

        @NotNull(message = "Initial cash balance is required")
        @DecimalMin(value = "0.0", inclusive = true, message = "Initial cash balance cannot be negative")
        BigDecimal initialCashBalance
) {
}

record PortfolioResponse(
        Long id,
        String portfolioName,
        BigDecimal cashBalance,
        BigDecimal holdingsValue,
        BigDecimal totalPortfolioValue,
        Long clientId,
        String clientName,
        LocalDateTime createdAt
) {
}

record TradeRequest(
        @NotNull(message = "Portfolio id is required")
        Long portfolioId,

        @NotBlank(message = "Symbol is required")
        String symbol,

        @NotNull(message = "Trade type is required")
        TradeType tradeType,

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be greater than zero")
        Integer quantity,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", inclusive = true, message = "Price must be greater than zero")
        BigDecimal price
) {
    public String symbol() {
        return symbol == null ? null : symbol.toUpperCase();
    }
}

record TradeResponse(
        Long id,
        Long portfolioId,
        String symbol,
        TradeType tradeType,
        Integer quantity,
        BigDecimal price,
        BigDecimal totalAmount,
        LocalDateTime executedAt
) {
}

record HoldingResponse(
        Long id,
        String symbol,
        Integer quantity,
        BigDecimal averageCost,
        BigDecimal marketValue,
        LocalDateTime updatedAt
) {
}

record AuditLogResponse(
        Long id,
        String action,
        String entityType,
        Long entityId,
        String description,
        LocalDateTime createdAt
) {
}

/* =====================================================
   REPOSITORIES
   ===================================================== */

interface ClientRepository extends JpaRepository<Client, Long> {
    Optional<Client> findByEmail(String email);
}

interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findByClientId(Long clientId);
}

interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByPortfolioIdAndSymbol(Long portfolioId, String symbol);

    List<Holding> findByPortfolioId(Long portfolioId);
}

interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findByPortfolioIdOrderByExecutedAtDesc(Long portfolioId);
}

interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findTop50ByOrderByCreatedAtDesc();
}

/* =====================================================
   SERVICES
   ===================================================== */

@Service
class AuditService {

    private final AuditLogRepository auditLogRepository;

    AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void record(String action, String entityType, Long entityId, String description) {
        auditLogRepository.save(new AuditLog(action, entityType, entityId, description));
    }

    public List<AuditLogResponse> getRecentAuditLogs() {
        return auditLogRepository.findTop50ByOrderByCreatedAtDesc()
                .stream()
                .map(log -> new AuditLogResponse(
                        log.getId(),
                        log.getAction(),
                        log.getEntityType(),
                        log.getEntityId(),
                        log.getDescription(),
                        log.getCreatedAt()
                ))
                .toList();
    }
}

@Service
class ClientService {

    private final ClientRepository clientRepository;
    private final AuditService auditService;

    ClientService(ClientRepository clientRepository, AuditService auditService) {
        this.clientRepository = clientRepository;
        this.auditService = auditService;
    }

    public ClientResponse createClient(CreateClientRequest request) {
        clientRepository.findByEmail(request.email()).ifPresent(client -> {
            throw new BadRequestException("Client already exists with email: " + request.email());
        });

        Client client = new Client(request.fullName(), request.email(), request.riskProfile());
        Client savedClient = clientRepository.save(client);

        auditService.record(
                "CREATE_CLIENT",
                "CLIENT",
                savedClient.getId(),
                "Created finance client profile"
        );

        return mapToResponse(savedClient);
    }

    public List<ClientResponse> getAllClients() {
        return clientRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public ClientResponse getClientById(Long id) {
        return mapToResponse(findClient(id));
    }

    public Client findClient(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    private ClientResponse mapToResponse(Client client) {
        return new ClientResponse(
                client.getId(),
                client.getFullName(),
                client.getEmail(),
                client.getRiskProfile(),
                client.getCreatedAt()
        );
    }
}

@Service
class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final HoldingRepository holdingRepository;
    private final ClientService clientService;
    private final AuditService auditService;

    PortfolioService(PortfolioRepository portfolioRepository,
                     HoldingRepository holdingRepository,
                     ClientService clientService,
                     AuditService auditService) {
        this.portfolioRepository = portfolioRepository;
        this.holdingRepository = holdingRepository;
        this.clientService = clientService;
        this.auditService = auditService;
    }

    public PortfolioResponse createPortfolio(CreatePortfolioRequest request) {
        Client client = clientService.findClient(request.clientId());

        Portfolio portfolio = new Portfolio(
                request.portfolioName(),
                request.initialCashBalance(),
                client
        );

        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        auditService.record(
                "CREATE_PORTFOLIO",
                "PORTFOLIO",
                savedPortfolio.getId(),
                "Created client investment portfolio"
        );

        return mapToResponse(savedPortfolio);
    }

    public List<PortfolioResponse> getAllPortfolios() {
        return portfolioRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public PortfolioResponse getPortfolioById(Long id) {
        return mapToResponse(findPortfolio(id));
    }

    public List<PortfolioResponse> getPortfoliosByClient(Long clientId) {
        clientService.findClient(clientId);

        return portfolioRepository.findByClientId(clientId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public List<HoldingResponse> getHoldings(Long portfolioId) {
        findPortfolio(portfolioId);

        return holdingRepository.findByPortfolioId(portfolioId)
                .stream()
                .map(this::mapToHoldingResponse)
                .toList();
    }

    public Portfolio findPortfolio(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio not found with id: " + id));
    }

    public Portfolio savePortfolio(Portfolio portfolio) {
        return portfolioRepository.save(portfolio);
    }

    private PortfolioResponse mapToResponse(Portfolio portfolio) {
        BigDecimal holdingsValue = calculateHoldingsValue(portfolio.getId());
        BigDecimal totalPortfolioValue = portfolio.getCashBalance().add(holdingsValue);

        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getPortfolioName(),
                portfolio.getCashBalance(),
                holdingsValue,
                totalPortfolioValue,
                portfolio.getClient().getId(),
                portfolio.getClient().getFullName(),
                portfolio.getCreatedAt()
        );
    }

    private HoldingResponse mapToHoldingResponse(Holding holding) {
        BigDecimal marketValue = holding.getAverageCost()
                .multiply(BigDecimal.valueOf(holding.getQuantity()));

        return new HoldingResponse(
                holding.getId(),
                holding.getSymbol(),
                holding.getQuantity(),
                holding.getAverageCost(),
                marketValue,
                holding.getUpdatedAt()
        );
    }

    private BigDecimal calculateHoldingsValue(Long portfolioId) {
        return holdingRepository.findByPortfolioId(portfolioId)
                .stream()
                .map(holding -> holding.getAverageCost()
                        .multiply(BigDecimal.valueOf(holding.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

@Service
class TradeService {

    private final TradeRepository tradeRepository;
    private final HoldingRepository holdingRepository;
    private final PortfolioService portfolioService;
    private final AuditService auditService;

    TradeService(TradeRepository tradeRepository,
                 HoldingRepository holdingRepository,
                 PortfolioService portfolioService,
                 AuditService auditService) {
        this.tradeRepository = tradeRepository;
        this.holdingRepository = holdingRepository;
        this.portfolioService = portfolioService;
        this.auditService = auditService;
    }

    @Transactional
    public TradeResponse executeTrade(TradeRequest request) {
        Portfolio portfolio = portfolioService.findPortfolio(request.portfolioId());
        BigDecimal totalAmount = request.price()
                .multiply(BigDecimal.valueOf(request.quantity()));

        if (request.tradeType() == TradeType.BUY) {
            executeBuy(portfolio, request, totalAmount);
        } else {
            executeSell(portfolio, request, totalAmount);
        }

        Trade trade = new Trade(
                request.symbol(),
                request.tradeType(),
                request.quantity(),
                request.price(),
                totalAmount,
                portfolio
        );

        Trade savedTrade = tradeRepository.save(trade);

        auditService.record(
                "EXECUTE_TRADE",
                "TRADE",
                savedTrade.getId(),
                request.tradeType() + " trade executed for " + request.symbol()
        );

        return mapToResponse(savedTrade, portfolio.getId());
    }

    public List<TradeResponse> getTradesByPortfolio(Long portfolioId) {
        portfolioService.findPortfolio(portfolioId);

        return tradeRepository.findByPortfolioIdOrderByExecutedAtDesc(portfolioId)
                .stream()
                .map(trade -> mapToResponse(trade, portfolioId))
                .toList();
    }

    private void executeBuy(Portfolio portfolio, TradeRequest request, BigDecimal totalAmount) {
        if (portfolio.getCashBalance().compareTo(totalAmount) < 0) {
            throw new BadRequestException("Insufficient cash balance for BUY trade");
        }

        Holding holding = holdingRepository
                .findByPortfolioIdAndSymbol(portfolio.getId(), request.symbol())
                .orElse(new Holding(request.symbol(), 0, BigDecimal.ZERO, portfolio));

        int existingQuantity = holding.getQuantity();
        int newQuantity = existingQuantity + request.quantity();

        BigDecimal existingValue = holding.getAverageCost()
                .multiply(BigDecimal.valueOf(existingQuantity));

        BigDecimal newAverageCost = existingValue
                .add(totalAmount)
                .divide(BigDecimal.valueOf(newQuantity), 2, RoundingMode.HALF_UP);

        holding.setQuantity(newQuantity);
        holding.setAverageCost(newAverageCost);
        holdingRepository.save(holding);

        portfolio.setCashBalance(portfolio.getCashBalance().subtract(totalAmount));
        portfolioService.savePortfolio(portfolio);
    }

    private void executeSell(Portfolio portfolio, TradeRequest request, BigDecimal totalAmount) {
        Holding holding = holdingRepository
                .findByPortfolioIdAndSymbol(portfolio.getId(), request.symbol())
                .orElseThrow(() -> new BadRequestException(
                        "No holdings available for symbol: " + request.symbol()
                ));

        if (holding.getQuantity() < request.quantity()) {
            throw new BadRequestException("Insufficient holdings for SELL trade");
        }

        holding.setQuantity(holding.getQuantity() - request.quantity());

        if (holding.getQuantity() == 0) {
            holdingRepository.delete(holding);
        } else {
            holdingRepository.save(holding);
        }

        portfolio.setCashBalance(portfolio.getCashBalance().add(totalAmount));
        portfolioService.savePortfolio(portfolio);
    }

    private TradeResponse mapToResponse(Trade trade, Long portfolioId) {
        return new TradeResponse(
                trade.getId(),
                portfolioId,
                trade.getSymbol(),
                trade.getTradeType(),
                trade.getQuantity(),
                trade.getPrice(),
                trade.getTotalAmount(),
                trade.getExecutedAt()
        );
    }
}

/* =====================================================
   CONTROLLERS
   ===================================================== */

@RestController
@RequestMapping("/api/clients")
@CrossOrigin(origins = "*")
class ClientController {

    private final ClientService clientService;

    ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClientResponse createClient(@Valid @RequestBody CreateClientRequest request) {
        return clientService.createClient(request);
    }

    @GetMapping
    public List<ClientResponse> getAllClients() {
        return clientService.getAllClients();
    }

    @GetMapping("/{id}")
    public ClientResponse getClientById(@PathVariable Long id) {
        return clientService.getClientById(id);
    }
}

@RestController
@RequestMapping("/api/portfolios")
@CrossOrigin(origins = "*")
class PortfolioController {

    private final PortfolioService portfolioService;

    PortfolioController(PortfolioService portfolioService) {
        this.portfolioService = portfolioService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PortfolioResponse createPortfolio(@Valid @RequestBody CreatePortfolioRequest request) {
        return portfolioService.createPortfolio(request);
    }

    @GetMapping
    public List<PortfolioResponse> getAllPortfolios() {
        return portfolioService.getAllPortfolios();
    }

    @GetMapping("/{id}")
    public PortfolioResponse getPortfolioById(@PathVariable Long id) {
        return portfolioService.getPortfolioById(id);
    }

    @GetMapping("/client/{clientId}")
    public List<PortfolioResponse> getPortfoliosByClient(@PathVariable Long clientId) {
        return portfolioService.getPortfoliosByClient(clientId);
    }

    @GetMapping("/{portfolioId}/holdings")
    public List<HoldingResponse> getHoldings(@PathVariable Long portfolioId) {
        return portfolioService.getHoldings(portfolioId);
    }
}

@RestController
@RequestMapping("/api/trades")
@CrossOrigin(origins = "*")
class TradeController {

    private final TradeService tradeService;

    TradeController(TradeService tradeService) {
        this.tradeService = tradeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TradeResponse executeTrade(@Valid @RequestBody TradeRequest request) {
        return tradeService.executeTrade(request);
    }

    @GetMapping("/portfolio/{portfolioId}")
    public List<TradeResponse> getTradesByPortfolio(@PathVariable Long portfolioId) {
        return tradeService.getTradesByPortfolio(portfolioId);
    }
}

@RestController
@RequestMapping("/api/audit-logs")
@CrossOrigin(origins = "*")
class AuditController {

    private final AuditService auditService;

    AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public List<AuditLogResponse> getRecentAuditLogs() {
        return auditService.getRecentAuditLogs();
    }
}

/* =====================================================
   EXCEPTIONS
   ===================================================== */

class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}

class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> validationErrors = new HashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(error -> validationErrors.put(error.getField(), error.getDefaultMessage()));

        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation failed");
        response.put("details", validationErrors);

        return ResponseEntity.badRequest().body(response);
    }

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> response = new HashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", message);

        return ResponseEntity.status(status).body(response);
    }
}
