package com.agriconnect.auth.service;

import com.agriconnect.auth.dto.AuthResponse;
import com.agriconnect.auth.entity.Organization;
import com.agriconnect.auth.entity.Role;
import com.agriconnect.auth.entity.User;
import com.agriconnect.auth.repository.OrganizationRepository;
import com.agriconnect.auth.repository.UserRepository;
import com.agriconnect.common.exception.NotFoundException;
import com.agriconnect.farm.entity.Crop;
import com.agriconnect.farm.entity.CropStatus;
import com.agriconnect.farm.entity.Farm;
import com.agriconnect.farm.entity.Livestock;
import com.agriconnect.farm.repository.CropRepository;
import com.agriconnect.farm.repository.FarmRepository;
import com.agriconnect.farm.repository.LivestockRepository;
import com.agriconnect.finance.entity.Expense;
import com.agriconnect.finance.entity.Revenue;
import com.agriconnect.finance.repository.ExpenseRepository;
import com.agriconnect.finance.repository.RevenueRepository;
import com.agriconnect.inventory.entity.InventoryItem;
import com.agriconnect.inventory.entity.YieldRecord;
import com.agriconnect.inventory.repository.InventoryItemRepository;
import com.agriconnect.inventory.repository.YieldRecordRepository;
import com.agriconnect.marketplace.entity.Listing;
import com.agriconnect.marketplace.entity.ListingStatus;
import com.agriconnect.marketplace.entity.Order;
import com.agriconnect.marketplace.entity.OrderStatus;
import com.agriconnect.marketplace.repository.ListingRepository;
import com.agriconnect.marketplace.repository.OrderRepository;
import com.agriconnect.payment.entity.EscrowStatus;
import com.agriconnect.payment.entity.EscrowTransaction;
import com.agriconnect.payment.repository.EscrowTransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * One-click "Try the live demo" login. The first call seeds a seller org
 * (OWNER) and a buyer org (BUYER) with realistic data across every module,
 * including a PAID order whose escrow is HELD so the fulfil -> release flow
 * can be shown. Later calls just issue a token for the existing demo user.
 *
 * Demo users get a random password nobody knows; access is only through this
 * endpoint, which can be switched off with app.demo.enabled=false.
 */
@Service
public class DemoService {

    static final String SELLER_EMAIL = "demo-farmer@agriconnect.demo";
    static final String BUYER_EMAIL = "demo-buyer@agriconnect.demo";

    private final boolean enabled;
    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final FarmRepository farmRepository;
    private final CropRepository cropRepository;
    private final LivestockRepository livestockRepository;
    private final InventoryItemRepository inventoryItemRepository;
    private final YieldRecordRepository yieldRecordRepository;
    private final ExpenseRepository expenseRepository;
    private final RevenueRepository revenueRepository;
    private final ListingRepository listingRepository;
    private final OrderRepository orderRepository;
    private final EscrowTransactionRepository escrowTransactionRepository;

    public DemoService(@Value("${app.demo.enabled:true}") boolean enabled,
                       UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       FarmRepository farmRepository,
                       CropRepository cropRepository,
                       LivestockRepository livestockRepository,
                       InventoryItemRepository inventoryItemRepository,
                       YieldRecordRepository yieldRecordRepository,
                       ExpenseRepository expenseRepository,
                       RevenueRepository revenueRepository,
                       ListingRepository listingRepository,
                       OrderRepository orderRepository,
                       EscrowTransactionRepository escrowTransactionRepository) {
        this.enabled = enabled;
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.farmRepository = farmRepository;
        this.cropRepository = cropRepository;
        this.livestockRepository = livestockRepository;
        this.inventoryItemRepository = inventoryItemRepository;
        this.yieldRecordRepository = yieldRecordRepository;
        this.expenseRepository = expenseRepository;
        this.revenueRepository = revenueRepository;
        this.listingRepository = listingRepository;
        this.orderRepository = orderRepository;
        this.escrowTransactionRepository = escrowTransactionRepository;
    }

    /**
     * Logs in as the demo farmer (default) or, with asBuyer, the demo buyer.
     * synchronized so two simultaneous first visits don't both seed.
     */
    @Transactional
    public synchronized AuthResponse login(boolean asBuyer) {
        if (!enabled) {
            throw new NotFoundException("Demo is disabled");
        }
        if (!userRepository.existsByEmail(SELLER_EMAIL)) {
            seed();
        }
        User user = userRepository.findByEmail(asBuyer ? BUYER_EMAIL : SELLER_EMAIL)
                .orElseThrow(() -> new NotFoundException("Demo user missing"));
        Organization org = user.getOrganization();
        String token = jwtService.generateToken(user.getId(), user.getEmail(), org.getId(), user.getRole().name());
        return new AuthResponse(token, "Bearer", user.getId(), user.getEmail(), user.getFullName(),
                user.getRole().name(), org.getId(), org.getName());
    }

    private void seed() {
        LocalDate today = LocalDate.now();
        Long seller = createOrgWithUser("Green Valley Co-op (Demo)", "green-valley-co-op-demo",
                SELLER_EMAIL, "Amina Bello", Role.OWNER);
        Long buyer = createOrgWithUser("Lagos Fresh Market (Demo)", "lagos-fresh-market-demo",
                BUYER_EMAIL, "Tunde Okafor", Role.BUYER);

        Farm main = farm(seller, "Green Valley Main", "Ibadan", 12.5);
        Farm riverside = farm(seller, "Riverside Plot", "Abeokuta", 4.0);
        Farm northern = farm(seller, "Northern Field", "Kano", 20.0);

        Crop maize = crop(seller, main, "Maize", today.minusDays(70), today.plusDays(30), CropStatus.GROWING);
        crop(seller, main, "Cassava", today.minusDays(20), today.plusDays(250), CropStatus.PLANTED);
        Crop tomatoes = crop(seller, riverside, "Tomatoes", today.minusDays(110), today.minusDays(20), CropStatus.HARVESTED);
        crop(seller, northern, "Yam", today.minusDays(90), today.plusDays(120), CropStatus.GROWING);
        crop(seller, northern, "Sorghum", today.minusDays(60), today.plusDays(45), CropStatus.GROWING);

        livestock(seller, main, "Goat", "West African Dwarf", 24, "Vaccinated in September");
        livestock(seller, riverside, "Poultry", "Broiler", 300, "Healthy, 5 weeks old");

        inventory(seller, main, "Maize grain", "Produce", 1200.0);
        inventory(seller, main, "NPK fertilizer", "Input", 350.0);
        inventory(seller, riverside, "Tomatoes", "Produce", 480.0);

        yieldRecord(seller, tomatoes, today.minusDays(20), 1850.0);
        yieldRecord(seller, maize, today.minusDays(200), 2600.0);

        expense(seller, main, "Seeds", 85000, "Improved maize and cassava seed", today.minusDays(72));
        expense(seller, main, "Fertilizer", 120000, "NPK 15-15-15, 10 bags", today.minusDays(50));
        expense(seller, northern, "Labour", 150000, "Weeding and ridging", today.minusDays(35));
        expense(seller, riverside, "Veterinary", 30000, "Poultry vaccines", today.minusDays(12));

        revenue(seller, riverside, "Tomato sales", 420000, today.minusDays(15));
        revenue(seller, riverside, "Egg sales", 95000, today.minusDays(8));
        revenue(seller, main, "Maize sales", 260000, today.minusDays(190));

        Listing tomatoListing = listing(seller, riverside, "Fresh tomatoes", "Firm Roma tomatoes, picked this week",
                2500, "kg", 400);
        Listing maizeListing = listing(seller, main, "Yellow maize", "Dry, cleaned and bagged", 850, "kg", 1000);
        listing(seller, main, "Live goats", "West African Dwarf, 8-12 months", 65000, "head", 10);

        // Paid order with funds held in escrow: the seller can mark it
        // fulfilled and release the funds during the demo.
        Order paid = order(tomatoListing, buyer, seller, 50, OrderStatus.PAID);
        EscrowTransaction escrow = new EscrowTransaction();
        escrow.setOrderId(paid.getId());
        escrow.setBuyerOrganizationId(buyer);
        escrow.setAmount(paid.getTotalAmount());
        escrow.setPaystackReference("DEMO-" + UUID.randomUUID());
        escrow.setStatus(EscrowStatus.HELD);
        escrowTransactionRepository.save(escrow);

        order(maizeListing, buyer, seller, 200, OrderStatus.PENDING_PAYMENT);
    }

    private Long createOrgWithUser(String orgName, String slug, String email, String fullName, Role role) {
        Organization org = new Organization();
        org.setName(orgName);
        org.setSlug(slug);
        org = organizationRepository.save(org);

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setFullName(fullName);
        user.setRole(role);
        user.setOrganization(org);
        userRepository.save(user);
        return org.getId();
    }

    private Farm farm(Long tenant, String name, String location, double hectares) {
        Farm farm = new Farm();
        farm.setTenantId(tenant);
        farm.setName(name);
        farm.setLocation(location);
        farm.setSizeHectares(hectares);
        return farmRepository.save(farm);
    }

    private Crop crop(Long tenant, Farm farm, String name, LocalDate planted, LocalDate harvest, CropStatus status) {
        Crop crop = new Crop();
        crop.setTenantId(tenant);
        crop.setFarm(farm);
        crop.setCropName(name);
        crop.setPlantingDate(planted);
        crop.setExpectedHarvestDate(harvest);
        crop.setStatus(status);
        return cropRepository.save(crop);
    }

    private void livestock(Long tenant, Farm farm, String species, String breed, int quantity, String notes) {
        Livestock livestock = new Livestock();
        livestock.setTenantId(tenant);
        livestock.setFarm(farm);
        livestock.setSpecies(species);
        livestock.setBreed(breed);
        livestock.setQuantity(quantity);
        livestock.setHealthNotes(notes);
        livestockRepository.save(livestock);
    }

    private void inventory(Long tenant, Farm farm, String name, String category, double kg) {
        InventoryItem item = new InventoryItem();
        item.setTenantId(tenant);
        item.setFarm(farm);
        item.setItemName(name);
        item.setCategory(category);
        item.setQuantityKg(kg);
        inventoryItemRepository.save(item);
    }

    private void yieldRecord(Long tenant, Crop crop, LocalDate date, double kg) {
        YieldRecord record = new YieldRecord();
        record.setTenantId(tenant);
        record.setCrop(crop);
        record.setHarvestDate(date);
        record.setQuantityKg(kg);
        yieldRecordRepository.save(record);
    }

    private void expense(Long tenant, Farm farm, String category, long amount, String description, LocalDate date) {
        Expense expense = new Expense();
        expense.setTenantId(tenant);
        expense.setFarm(farm);
        expense.setCategory(category);
        expense.setAmount(BigDecimal.valueOf(amount));
        expense.setDescription(description);
        expense.setDate(date);
        expenseRepository.save(expense);
    }

    private void revenue(Long tenant, Farm farm, String source, long amount, LocalDate date) {
        Revenue revenue = new Revenue();
        revenue.setTenantId(tenant);
        revenue.setFarm(farm);
        revenue.setSource(source);
        revenue.setAmount(BigDecimal.valueOf(amount));
        revenue.setDate(date);
        revenueRepository.save(revenue);
    }

    private Listing listing(Long org, Farm farm, String produce, String description, long price, String unit, long quantity) {
        Listing listing = new Listing();
        listing.setOrganizationId(org);
        listing.setFarmId(farm.getId());
        listing.setProduceName(produce);
        listing.setDescription(description);
        listing.setPricePerUnit(BigDecimal.valueOf(price));
        listing.setUnit(unit);
        listing.setQuantityAvailable(BigDecimal.valueOf(quantity));
        listing.setStatus(ListingStatus.ACTIVE);
        return listingRepository.save(listing);
    }

    private Order order(Listing listing, Long buyer, Long seller, long quantity, OrderStatus status) {
        Order order = new Order();
        order.setListingId(listing.getId());
        order.setBuyerOrganizationId(buyer);
        order.setSellerOrganizationId(seller);
        order.setQuantity(BigDecimal.valueOf(quantity));
        order.setTotalAmount(listing.getPricePerUnit().multiply(BigDecimal.valueOf(quantity)));
        order.setStatus(status);
        return orderRepository.save(order);
    }
}
